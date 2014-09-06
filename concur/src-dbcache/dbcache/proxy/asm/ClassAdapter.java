package dbcache.proxy.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import dbcache.proxy.util.ClassUtil;

/**
 * (动态)生成静态代理类 同时继承被代理的类
 * 
 * @author Jake
 * @date 2014年9月6日上午12:06:47
 */
public class ClassAdapter extends ClassVisitor implements Opcodes {
	
	/** 构造方法名常量 */
	private static final String INIT = "<init>";

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
	 * @param enhancedClassName
	 *            代理类类名
	 * @param targetClass
	 *            被代理类
	 * @param writer
	 *            ClassWriter
	 */
	public ClassAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer) {
		super(Opcodes.ASM4, writer);
		this.classWriter = writer;
		this.originalClassName = targetClass.getName();
		this.enhancedClassName = enhancedClassName;
		this.originalClass = targetClass;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, Opcodes.ACC_PUBLIC,
				ClassUtil.toAsmCls(enhancedClassName), signature, name,
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
		// 清除所有属性
		return null;
	}

	/**
	 * 
	 * <p>
	 * 前置方法
	 * </p>
	 *
	 * @see TxHandler
	 * @param mWriter
	 */
	private static void doBefore(MethodVisitor mWriter, String methodInfo) {
		mWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		mWriter.visitLdcInsn("before method : " + methodInfo);
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
				"println", "(Ljava/lang/String;)V");
		// 或者直接调用静态方法
		// mWriter.visitLdcInsn(methodInfo);
		// mWriter.visitMethodInsn(INVOKESTATIC,toAsmCls(TxHandler.class.getName()),"before","(Ljava/lang/String;)V");

	}

	/**
	 * 
	 * <p>
	 * 后置方法
	 * </p>
	 * 
	 * @see TxHandler
	 * @param mWriter
	 */
	private static void doAfter(MethodVisitor mWriter, String methodInfo) {
		mWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		mWriter.visitLdcInsn("after method : " + methodInfo);
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
				"println", "(Ljava/lang/String;)V");
	}

	@Override
	public void visitEnd() {

		// 增加原实体类型的属性(真实类)
		classWriter.visitField(Opcodes.ACC_PROTECTED, "obj",
				Type.getDescriptor(originalClass), null, null);

		// 调用originalClassName的<init>方法，否则class不能实例化
		MethodVisitor mvInit = classWriter.visitMethod(ACC_PUBLIC, INIT, "()V",
				null, null);
		mvInit.visitVarInsn(ALOAD, 0);
		mvInit.visitMethodInsn(INVOKESPECIAL,
				ClassUtil.toAsmCls(originalClassName), INIT, "()V");
		mvInit.visitInsn(RETURN);
		mvInit.visitMaxs(0, 0);
		mvInit.visitEnd();

		// 添加带参构造方法,用真实类对象作为参数
		MethodVisitor mvInit1 = classWriter.visitMethod(ACC_PUBLIC, INIT, "("
				+ Type.getDescriptor(originalClass) + ")V", null, null);
		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);

		mvInit1.visitMethodInsn(INVOKESPECIAL,
				ClassUtil.toAsmCls(originalClassName), INIT, "()V");

		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit1.visitVarInsn(Opcodes.ALOAD, 1);

		mvInit1.visitFieldInsn(Opcodes.PUTFIELD,
				ClassUtil.toAsmCls(enhancedClassName), "obj",
				Type.getDescriptor(originalClass));

		mvInit1.visitInsn(RETURN);
		mvInit1.visitMaxs(3, 2);
		mvInit1.visitEnd();

		// 获取所有方法，并重写(main方法 和 Object的方法除外)
		Method[] methods = originalClass.getMethods();
		for (Method m : methods) {
			if (!ClassUtil.needOverride(m)) {
				continue;
			}
			Type mt = Type.getType(m);

			StringBuilder methodInfo = new StringBuilder(originalClassName);
			methodInfo.append(".").append(m.getName());
			methodInfo.append("|");

			Class<?>[] paramTypes = m.getParameterTypes();
			for (Class<?> t : paramTypes) {
				methodInfo.append(t.getName()).append(",");
			}
			if (paramTypes.length > 0) {
				methodInfo.deleteCharAt(methodInfo.length() - 1);
			}

			// 方法是被哪个类定义的
			String declaringCls = ClassUtil.toAsmCls(m.getDeclaringClass()
					.getName());

			// 方法 description
			MethodVisitor mWriter = classWriter.visitMethod(ACC_PUBLIC,
					m.getName(), mt.toString(), null, null);

			// insert code here (before)
			doBefore(mWriter, methodInfo.toString());

			// 如果不是静态方法 load this.obj对象
			if (!Modifier.isStatic(m.getModifiers())) {
				mWriter.visitVarInsn(Opcodes.ALOAD, 0);
				mWriter.visitFieldInsn(Opcodes.GETFIELD,
						ClassUtil.toAsmCls(enhancedClassName), "obj",
						Type.getDescriptor(originalClass));
			}

			int i = 1;
			// load 出方法的所有参数
			for (Class<?> tCls : m.getParameterTypes()) {
				Type t = Type.getType(tCls);
				mWriter.visitVarInsn(ClassUtil.loadCode(t), i++);
				// long和double 用64位表示，要后移一个位置，否则会报错
				if (t.getSort() == Type.LONG || t.getSort() == Type.DOUBLE) {
					i++;
				}
			}

			// this.obj.xxx();
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					ClassUtil.toAsmCls(declaringCls), m.getName(),
					mt.toString());

			// 处理返回值类型
			Type rt = Type.getReturnType(m);
			// 没有返回值
			if (rt.toString().equals("V")) {
				doAfter(mWriter, methodInfo.toString());
				mWriter.visitInsn(RETURN);
			}
			// 把return xxx() 转变成 ： Object o = xxx(); return o;
			else {
				int storeCode = ClassUtil.storeCode(rt);
				int loadCode = ClassUtil.loadCode(rt);
				int returnCode = ClassUtil.rtCode(rt);

				mWriter.visitVarInsn(storeCode, i);
				doAfter(mWriter, methodInfo.toString());
				mWriter.visitVarInsn(loadCode, i);
				mWriter.visitInsn(returnCode);
			}

			// 已设置了自动计算，但还是要调用一下，不然会报错
			mWriter.visitMaxs(i, ++i);
			mWriter.visitEnd();
		}
		cv.visitEnd();
	}

}
