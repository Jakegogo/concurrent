package org.cc.demo2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 
 * <p>根据class A生成一个class B extends A</p>
 * <li>重写A及A父类的所有方法，eg. public void xx() {super.xx();}
 * <li>copy A类定义的所有属性
 * 
 * @author dixingxing	
 * @date May 3, 2012
 */
public class ClassAdapter extends ClassVisitor implements Opcodes{
	public static final String INIT = "<init>";
	private ClassWriter classWriter;
	private String originalClassName;
	private String enhancedClassName;
	private Class<?> originalClass;

	public ClassAdapter(String enhancedClassName,
			Class<?> targetClass, ClassWriter writer) {
		super(Opcodes.ASM4,writer);
		this.classWriter = writer;
		this.originalClassName = targetClass.getName();
		this.enhancedClassName = enhancedClassName;
		this.originalClass = targetClass;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, Opcodes.ACC_PUBLIC,
				toAsmCls(enhancedClassName), signature, name,
				interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return null;
//		if(name.equals("num")) {
//			return null;
//		}
//		// 拷贝所有属性   可使用java反射给属性赋值（生成class后newInstance在赋值）
//		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return null;
//		// 删除所有set方法
//		if(name.startsWith("set")) {
//			return null;
//		}
//		//删除默认构造方法
//		if(name.equals("<init>")) {
//			return null;
//		}
//		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	/**
	 * 把类名中的.替换为/
	 * @param className
	 * @return
	 */
	private static String toAsmCls(String className) {
		return className.replace('.', '/');
	}
	
	/**
	 * 
	 * <p>前置方法</p>
	 *
	 * @see TxHandler
	 * @param mWriter
	 */
	private static void doBefore(MethodVisitor mWriter,String methodInfo) {
		mWriter.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");  
		mWriter.visitLdcInsn("before method : " + methodInfo);   
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",   
	        "(Ljava/lang/String;)V");  	
		// 或者直接调用静态方法
		// mWriter.visitLdcInsn(methodInfo);
		// mWriter.visitMethodInsn(INVOKESTATIC,toAsmCls(TxHandler.class.getName()),"before","(Ljava/lang/String;)V");

	}
	
	
	
	/**
	 * 
	 * <p>后置方法</p>
	 * @see TxHandler
	 * @param mWriter
	 */
	private static void doAfter(MethodVisitor mWriter,String methodInfo) {
		mWriter.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");  
		mWriter.visitLdcInsn("after method : " + methodInfo);   
		mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",   
	        "(Ljava/lang/String;)V");  
	}
	
	
	/**
	 * 
	 * <p>
	 * object类本身的方法不做重写
	 * </p>
	 * <p>
	 * "main" 方法不做重写
	 * </p>
	 * 
	 * @param m
	 * @return
	 */
	public static boolean needOverride(Method m) {
		// object类本身的方法不做重写
		if (m.getDeclaringClass().getName().equals(Object.class.getName())) {
			return false;
		}
		// "main" 方法不做重写
		if (Modifier.isPublic(m.getModifiers())
				&& Modifier.isStatic(m.getModifiers())
				&& m.getReturnType().getName().equals("void")
				&& m.getName().equals("main")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void visitEnd() {
		
		classWriter.visitField(Opcodes.ACC_PROTECTED, "obj", Type.getDescriptor(Entity.class),  
                null, null);
		
		// 如果originalClass定义了私有成员变量，那么直接在visitMethod中复制originalClass的<init>会报错。
//		ALOAD 0
//		INVOKESPECIAL cc/RoleService.<init>()V
//		RETURN
//		// 调用originalClassName的<init>方法，否则class不能实例化
		MethodVisitor mvInit = classWriter.visitMethod(ACC_PUBLIC, INIT, "()V", null, null); 
		mvInit.visitVarInsn(ALOAD, 0);
		mvInit.visitMethodInsn(INVOKESPECIAL, toAsmCls(originalClassName), INIT, "()V");
		mvInit.visitInsn(RETURN);
		mvInit.visitMaxs(0, 0);
		mvInit.visitEnd();
		
		//添加带参构造方法
		MethodVisitor mvInit1 = classWriter.visitMethod(ACC_PUBLIC, INIT, "(" + Type.getDescriptor(Entity.class) + ")V", null, null); 
		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);
		
		mvInit1.visitMethodInsn(INVOKESPECIAL, toAsmCls(originalClassName), INIT, "()V");
		
		mvInit1.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit1.visitVarInsn(Opcodes.ALOAD, 1);
		
		mvInit1.visitFieldInsn(Opcodes.PUTFIELD, toAsmCls(enhancedClassName), "obj", Type.getDescriptor(Entity.class));
		
		mvInit1.visitInsn(RETURN);
		mvInit1.visitMaxs(3, 2);
		mvInit1.visitEnd();
		
		
		// 获取所有方法，并重写(main方法 和 Object的方法除外)
		Method[] methods = originalClass.getMethods();
		for(Method m : methods) {
			if(!needOverride(m)) {
				continue;
			}
			Type mt = Type.getType(m);

			StringBuilder methodInfo = new StringBuilder(originalClassName);
			methodInfo.append(".").append(m.getName());
			methodInfo.append("|");
			
			Class<?>[] paramTypes = m.getParameterTypes();
			for(Class<?> t : paramTypes) {
				methodInfo.append(t.getName()).append(",");
			} 
			if(paramTypes.length > 0) {
				methodInfo.deleteCharAt(methodInfo.length() - 1);
			}
			
			// 方法是被哪个类定义的
			String declaringCls = toAsmCls(m.getDeclaringClass().getName());
			
			// 方法 description
			MethodVisitor mWriter = classWriter.visitMethod(ACC_PUBLIC, m.getName(), mt.toString(), null, null); 
			
			// insert code here (before)
			doBefore(mWriter, methodInfo.toString());
			
			// 如果不是静态方法 load this.obj对象
			if(!Modifier.isStatic(m.getModifiers())) {
				mWriter.visitVarInsn(Opcodes.ALOAD, 0);
				mWriter.visitFieldInsn(Opcodes.GETFIELD, toAsmCls(enhancedClassName), "obj", Type.getDescriptor(Entity.class));
			}
			
			int i = 1;
			// load 出方法的所有参数
			for(Class<?> tCls : m.getParameterTypes()) {
				Type t = Type.getType(tCls);
				mWriter.visitVarInsn(loadCode(t), i++);
				// long和double 用64位表示，要后移一个位置，否则会报错
				if(t.getSort() == Type.LONG || t.getSort() == Type.DOUBLE) {
					i++;
				}
			}
			
			
			// this.obj.xxx();
			mWriter.visitMethodInsn(INVOKEVIRTUAL, toAsmCls(declaringCls),m.getName(),mt.toString());
			
			// 处理返回值类型
			Type rt = Type.getReturnType(m);
			// 没有返回值
			if(rt.toString().equals("V")) {
				doAfter(mWriter,methodInfo.toString());
				mWriter.visitInsn(RETURN);
			}
			// 把return xxx() 转变成 ： Object o = xxx(); return o;
			else {
				int storeCode = storeCode(rt);
				int loadCode = loadCode(rt);
				int returnCode = rtCode(rt);
				
				mWriter.visitVarInsn(storeCode, i);
				doAfter(mWriter,methodInfo.toString());
				mWriter.visitVarInsn(loadCode, i);
				mWriter.visitInsn(returnCode);
			}
			
			// 已设置了自动计算，但还是要调用一下，不然会报错
	        mWriter.visitMaxs(i, ++i);
	        mWriter.visitEnd(); 
		}
		cv.visitEnd();
	}
	
	/**
	 * 
	 * <p>get StoreCode(Opcodes#xStore)</p>
	 *
	 *
	 * @param type
	 * @return
	 */
	public static int storeCode(Type type) {
		
		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ASTORE;
			break;
		case Type.BOOLEAN:
			sort = ISTORE;
			break;
		case Type.BYTE:
			sort = ISTORE;
			break;
		case Type.CHAR:
			sort = ISTORE;
			break;
		case Type.DOUBLE:
			sort = DSTORE;
			break;
		case Type.FLOAT:
			sort = FSTORE;
			break;
		case Type.INT:
			sort = ISTORE;
			break;
		case Type.LONG:
			sort = LSTORE;
			break;
		case Type.OBJECT:
			sort = ASTORE;
			break;
		case Type.SHORT:
			sort = ISTORE;
			break;
		default:
			break;
		}
		return sort;
	}
	
	/**
	 * 
	 *  <p>get StoreCode(Opcodes#xLOAD)</p>
	 *
	 * @param type
	 * @return
	 */
	public static int loadCode(Type type) {
		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ALOAD;
			break;
		case Type.BOOLEAN:
			sort = ILOAD;
			break;
		case Type.BYTE:
			sort = ILOAD;
			break;
		case Type.CHAR:
			sort = ILOAD;
			break;
		case Type.DOUBLE:
			sort = DLOAD;
			break;
		case Type.FLOAT:
			sort = FLOAD;
			break;
		case Type.INT:
			sort = ILOAD;
			break;
		case Type.LONG:
			sort = LLOAD;
			break;
		case Type.OBJECT:
			sort = ALOAD;
			break;
		case Type.SHORT:
			sort = ILOAD;
			break;
		default:
			break;
		}
		return sort;
	}
	
	/**
	 * 
	 *  <p>get StoreCode(Opcodes#xRETURN)</p>
	 *
	 * @param type
	 * @return
	 */
	public static int rtCode(Type type) {
		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ARETURN;
			break;
		case Type.BOOLEAN:
			sort = IRETURN;
			break;
		case Type.BYTE:
			sort = IRETURN;
			break;
		case Type.CHAR:
			sort = IRETURN;
			break;
		case Type.DOUBLE:
			sort = DRETURN;
			break;
		case Type.FLOAT:
			sort = FRETURN;
			break;
		case Type.INT:
			sort = IRETURN;
			break;
		case Type.LONG:
			sort = LRETURN;
			break;
		case Type.OBJECT:
			sort = ARETURN;
			break;
		case Type.SHORT:
			sort = IRETURN;
			break;
		default:
			break;
		}
		return sort;
	}
}
