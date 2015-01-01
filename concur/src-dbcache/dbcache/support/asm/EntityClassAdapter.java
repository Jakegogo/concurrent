package dbcache.support.asm;

import dbcache.utils.AsmUtils;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * (动态)生成静态代理类
 * <br/>继承被代理的类
 *
 * @author Jake
 * @date 2014年9月6日上午12:06:47
 */
public class EntityClassAdapter extends ClassVisitor implements Opcodes {

	/** 构造方法名常量 */
	private static final String INIT = "<init>";

	/** 真实对象的属性名 */
	public static final String REAL_OBJECT = "obj";

	/** 处理类对象的属性名 */
	public static final String HANDLER_OBJECT = "handler";

	/**
	 * 切面方法重写器
	 */
	private AbstractAsmMethodAspect methodAspect;

	/**
	 * ClassWriter
	 */
	private ClassWriter classWriter;

	/**
	 * 原始类
	 */
	private Class<?> originalClass;

	/**
	 * 原始类名
	 */
	private String originalClassName;

	/**
	 * 代理类名
	 */
	private String enhancedClassName;

	/**
	 * 构造方法
	 *
	 * @param enhancedClassName 代理类类名
	 * @param targetClass 被代理类
	 * @param writer  ClassWriter
	 */
	public EntityClassAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer) {
		this(enhancedClassName, targetClass, writer, new AbstractAsmMethodAspect() {});
	}

	/**
	 * 构造方法
	 * @param enhancedClassName 代理类类名
	 * @param targetClass 被代理类
	 * @param writer ClassWriter
	 * @param methodAspect 方法切面修改器
	 */
	public EntityClassAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer, AbstractAsmMethodAspect methodAspect) {
		super(Opcodes.ASM4, writer);
		this.classWriter = writer;
		this.originalClassName = targetClass.getName();
		this.enhancedClassName = enhancedClassName;
		this.originalClass = targetClass;
		this.methodAspect = methodAspect;
	}



	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// 清除类注解
		return null;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, Opcodes.ACC_PUBLIC,
				AsmUtils.toAsmCls(enhancedClassName), signature, name,
				interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		// 清除所有的属性
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		// 清除所有方法
		return null;
	}


	@Override
	public void visitEnd() {

		// 增加原实体类型的属性(真实类)
		classWriter.visitField(Opcodes.ACC_PROTECTED, REAL_OBJECT,
				Type.getDescriptor(originalClass), null, null);

		// 增加切面处理对象
		classWriter.visitField(Opcodes.ACC_PROTECTED, HANDLER_OBJECT,
				Type.getDescriptor(methodAspect.getAspectHandleClass()), null, null);

		// 调用originalClassName的<init>方法，否则class不能实例化
		MethodVisitor mvInit = classWriter.visitMethod(ACC_PUBLIC, INIT, "()V",
				null, null);
		mvInit.visitVarInsn(ALOAD, 0);
		mvInit.visitMethodInsn(INVOKESPECIAL,
				AsmUtils.toAsmCls(originalClassName), INIT, "()V");
		mvInit.visitInsn(RETURN);
		mvInit.visitMaxs(0, 0);
		mvInit.visitEnd();

		// 添加带参构造方法,用真实类对象作为参数
		MethodVisitor mvInit1 = classWriter.visitMethod(ACC_PUBLIC, INIT, "("
				+ Type.getDescriptor(originalClass) + ")V", null, null);
		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);

		mvInit1.visitMethodInsn(INVOKESPECIAL,
				AsmUtils.toAsmCls(originalClassName), INIT, "()V");

		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit1.visitVarInsn(Opcodes.ALOAD, 1);

		mvInit1.visitFieldInsn(Opcodes.PUTFIELD,
				AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT,
				Type.getDescriptor(originalClass));

		mvInit1.visitInsn(RETURN);
		mvInit1.visitMaxs(3, 2);
		mvInit1.visitEnd();


		// 添加真实对象和切面处理对象构造方法,用真实类对象作为参数
		MethodVisitor mvInit2 = classWriter.visitMethod(ACC_PUBLIC, INIT, "("
				+ Type.getDescriptor(originalClass)
				+ Type.getDescriptor(methodAspect.getAspectHandleClass()) + ")V", null, null);
		mvInit2.visitVarInsn(Opcodes.ALOAD, 0);

		mvInit2.visitMethodInsn(INVOKESPECIAL,
				AsmUtils.toAsmCls(originalClassName), INIT, "()V");

		mvInit2.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit2.visitVarInsn(Opcodes.ALOAD, 1);

		mvInit2.visitFieldInsn(Opcodes.PUTFIELD,
				AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT,
				Type.getDescriptor(originalClass));

		mvInit2.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit2.visitVarInsn(Opcodes.ALOAD, 2);

		mvInit2.visitFieldInsn(Opcodes.PUTFIELD,
				AsmUtils.toAsmCls(enhancedClassName), HANDLER_OBJECT,
				Type.getDescriptor(methodAspect.getAspectHandleClass()));

		mvInit2.visitInsn(RETURN);
		mvInit2.visitMaxs(3, 2);
		mvInit2.visitEnd();

		// 获取所有方法，并重写(main方法 和 Object的方法除外)
		Method[] methods = originalClass.getMethods();
		for (Method m : methods) {
			if (!AsmUtils.needOverride(m)) {
				continue;
			}
			Type mt = Type.getType(m);

			// 方法是被哪个类定义的
			String declaringCls = AsmUtils.toAsmCls(m.getDeclaringClass()
					.getName());

			// 方法 description
			MethodVisitor mWriter = classWriter.visitMethod(ACC_PUBLIC,
					m.getName(), mt.toString(), null, null);

			//统计当前maxLocals
			int i = 1;
			// 遍历方法的所有参数
			for (Class<?> tCls : m.getParameterTypes()) {
				Type t = Type.getType(tCls);
				i++;
				// long和double 用64位表示，要后移一个位置，否则会报错
				if (t.getSort() == Type.LONG || t.getSort() == Type.DOUBLE) {
					i++;
				}
			}

			// insert code here (before)
			int aspectBeforeLocalNum = this.methodAspect.doBefore(mWriter, m, i);

			// 如果不是静态方法 load this.obj对象
			if (!Modifier.isStatic(m.getModifiers())) {
				mWriter.visitVarInsn(Opcodes.ALOAD, 0);
				mWriter.visitFieldInsn(Opcodes.GETFIELD,
						AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT,
						Type.getDescriptor(originalClass));
			}

			i = 1;
			// load 出方法的所有参数
			for (Class<?> tCls : m.getParameterTypes()) {
				Type t = Type.getType(tCls);
				mWriter.visitVarInsn(AsmUtils.loadCode(t), i++);
				// long和double 用64位表示，要后移一个位置，否则会报错
				if (t.getSort() == Type.LONG || t.getSort() == Type.DOUBLE) {
					i++;
				}
			}

			// this.obj.xxx();
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					AsmUtils.toAsmCls(declaringCls), m.getName(),
					mt.toString());

			//doBefore 累加方法访问的本地变量数
			i = aspectBeforeLocalNum;

			int aspectAfterLocalNum = 0;
			// 处理返回值类型
			Type rt = Type.getReturnType(m);
			// 没有返回值
			if (rt.toString().equals("V")) {
				aspectAfterLocalNum = this.methodAspect.doAfter(mWriter, m, i);
				mWriter.visitInsn(RETURN);
			}
			// 把return xxx() 转变成 ： Object o = xxx(); return o;
			else {
				int storeCode = AsmUtils.storeCode(rt);
				int loadCode = AsmUtils.loadCode(rt);
				int returnCode = AsmUtils.rtCode(rt);

				mWriter.visitVarInsn(storeCode, i);
				aspectAfterLocalNum = this.methodAspect.doAfter(mWriter, m, i);
				mWriter.visitVarInsn(loadCode, i);
				mWriter.visitInsn(returnCode);
			}

			//doBefore 累加方法访问的本地变量数
			i = aspectAfterLocalNum;

			// 已设置了自动计算，但还是要调用一下，不然会报错
			mWriter.visitMaxs(i, ++i);
			mWriter.visitEnd();
		}
		cv.visitEnd();
	}

}
