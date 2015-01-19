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
public class EntityClassProxyAdapter extends ClassVisitor implements Opcodes {

	/** 构造方法名常量 */
	public static final String INIT = "<init>";

	/** 真实对象的属性名 */
	public static final String REAL_OBJECT = "obj";

	/** 处理类对象的属性名 */
	public static final String HANDLER_OBJECT = "handler";

	/** equals方法名 */
	public static final String EQUALS_METHOD = "equals";

	/**
	 * 切面方法重写器
	 */
	private AbstractAsmMethodProxyAspect methodAspect;

	/**
	 * ClassWriter
	 */
	private ClassWriter classWriter;

	/**
	 * 原始类
	 */
	private Class<?> originalClass;

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
	public EntityClassProxyAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer) {
		this(enhancedClassName, targetClass, writer, new AbstractAsmMethodProxyAspect() {});
	}

	/**
	 * 构造方法
	 * @param enhancedClassName 代理类类名
	 * @param targetClass 被代理类
	 * @param writer ClassWriter
	 * @param methodAspect 方法切面修改器
	 */
	public EntityClassProxyAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer, AbstractAsmMethodProxyAspect methodAspect) {
		super(Opcodes.ASM4, writer);
		this.classWriter = writer;
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

		// 获取所有方法，并重写(main方法 和 Object的方法除外)
		Method[] methods = originalClass.getMethods();
		for (Method m : methods) {
			
			if (!AsmUtils.needOverride(m)) {
				continue;
			}

			// 覆盖equals方法
			if (EQUALS_METHOD.equals(m.getName())) {
				this.buildEqualsMethod(m);
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
			int aspectBeforeLocalNum = this.methodAspect.doBefore(originalClass, mWriter, m, i, m.getName(), Opcodes.ACC_PUBLIC, null);

			// 调用被代理对象源方法
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

			// 处理返回值类型
			Type rt = Type.getReturnType(m);
			// 需要重写
			if (this.methodAspect.needOverride(originalClass, m)) {
				int aspectAfterLocalNum = 0;

				// 没有返回值
				if (rt.toString().equals("V")) {
					aspectAfterLocalNum = this.methodAspect.doAfter(originalClass, mWriter, m, i, m.getName(), Opcodes.ACC_PUBLIC, null);
					mWriter.visitInsn(RETURN);
				}
				// 把return xxx() 转变成 ： Object o = xxx(); return o;
				else {
					int storeCode = AsmUtils.storeCode(rt);
					int loadCode = AsmUtils.loadCode(rt);
					int returnCode = AsmUtils.rtCode(rt);

					mWriter.visitVarInsn(storeCode, i);
					aspectAfterLocalNum = this.methodAspect.doAfter(originalClass, mWriter, m, i, m.getName(), Opcodes.ACC_PUBLIC, null);
					mWriter.visitVarInsn(loadCode, i);
					mWriter.visitInsn(returnCode);
				}

				//doBefore 累加方法访问的本地变量数
				i = aspectAfterLocalNum;
			} else {
				// 没有返回值
				if (rt.toString().equals("V")) {
					mWriter.visitInsn(RETURN);
				} else {
					mWriter.visitInsn(AsmUtils.rtCode(rt));
				}
			}

			// 已设置了自动计算，但还是要调用一下，不然会报错
			mWriter.visitMaxs(i, ++i);
			mWriter.visitEnd();
		}
		cv.visitEnd();
		
	}

	// 构建equals方法
	private void buildEqualsMethod(Method m) {

		Type mt = Type.getType(m);

		// 方法是被哪个类定义的
		String declaringCls = AsmUtils.toAsmCls(m.getDeclaringClass()
				.getName());

		// 方法 description
		MethodVisitor mWriter = classWriter.visitMethod(ACC_PUBLIC,
				m.getName(), mt.toString(), null, null);

		// 处理返回值类型
		Type rt = Type.getReturnType(m);
		int returnCode = AsmUtils.rtCode(rt);

//		public boolean equals(Object paramObject)
//		{
//			if(paramObject.getClass() == EnhancedEntity.class) {
//				return this.obj.equals(((EnhancedEntity)paramObject).obj);
//			}

		mWriter.visitVarInsn(ALOAD, 1);
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		mWriter.visitVarInsn(ALOAD, 0);
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		Label l1 = new Label();
		mWriter.visitJumpInsn(IF_ACMPNE, l1);
		mWriter.visitVarInsn(ALOAD, 0);
		mWriter.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, Type.getDescriptor(originalClass));
		mWriter.visitVarInsn(ALOAD, 1);
		mWriter.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(enhancedClassName));
		mWriter.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, Type.getDescriptor(originalClass));
		mWriter.visitMethodInsn(INVOKEVIRTUAL, AsmUtils.toAsmCls(originalClass.getName()), "equals", "(Ljava/lang/Object;)Z", false);
		mWriter.visitInsn(returnCode);
		mWriter.visitLabel(l1);

		//			return this.obj.equals(paramObject);
//		}

		mWriter.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

		mWriter.visitVarInsn(Opcodes.ALOAD, 0);
		mWriter.visitFieldInsn(Opcodes.GETFIELD,
				AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT,
				Type.getDescriptor(originalClass));

		int i = 1;
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

		mWriter.visitInsn(returnCode);

		// 已设置了自动计算，但还是要调用一下，不然会报错
		mWriter.visitMaxs(i, ++i);
		mWriter.visitEnd();
	}

}
