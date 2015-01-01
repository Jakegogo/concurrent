package dbcache.support.asm;

import dbcache.utils.AsmUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;


/**
 * ASM读取object属性工具类
 * @author Jake
 * @date 2014年11月5日上午1:05:14
 */
public class AsmAccessHelper implements Opcodes {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(AsmAccessHelper.class);

	/** 获取的方法名 */
	public static final String GETTER_METHOD_NAME = "get";

	/** 设置的方法名 */
	public static final String SETTER_METHOD_NAME = "set";

	/** 获取的方法名 */
	public static final String SET_TARGET_METHOD_NAME = "setTarget";

	/** 获取值名称 */
	public static final String GET_NAME_METHOD_NAME = "getName";

	/** 构造方法名常量 */
	private static final String INIT = "<init>";

	/** 代理类类名 */
	public static final String SUFIX = "$EnhancedByAsmFieldGetter";

	/** 代理类类名 */
	public static final String SUFIX1 = "$EnhancedByAsmFieldSetter";

	/** 分隔符 */
	public static final String SPLITER = "_";

	/** 真实对象引用 */
	protected static final String REAL_OBJECT = "target";

	/** 序号生成器 */
	private static AtomicLong id = new AtomicLong(0);

	/**
	 * 字节码类加载器
	 */
	public static AsmClassLoader classLoader = new AsmClassLoader();


	/**
	 * 创建属性获取器
	 * @param clazz 类
	 * @param field 属性
	 * @return ValueGetter<T>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> ValueGetter<T> createFieldGetter(final Class<T> clazz, final Field field) throws Exception {

		Class<T> enhancedClass = null;
		//代理类名
		final String enhancedClassName = AbstractFieldGetter.class.getName()
				+ SUFIX + SPLITER + clazz.getSimpleName() + SPLITER
				+ id.incrementAndGet();
		try {
			enhancedClass = (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(AbstractFieldGetter.class.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}


			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
			} catch (IntrospectionException e) {
				logger.error("无法获取get方法:" + clazz.getModifiers() + "(" + field.getName() + ")");
				e.printStackTrace();
			}
			//获取get方法
			final Method getMethod = propertyDescriptor.getReadMethod();
			final Type mt = Type.getType(getMethod);

			final String fieldGetterClassName = AsmUtils.toAsmCls(AbstractFieldGetter.class.getName());
			final String entityTypeString = Type.getDescriptor(clazz);
			final String fieldGetterTypeString = Type.getDescriptor(AbstractFieldGetter.class);
			final String fieldGetterTypeString1 = fieldGetterTypeString.substring(0, fieldGetterTypeString.length() - 1);

			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

				@Override
				public void visit(int version, int access, String name,
						String signature, String superName, String[] interfaces) {
					cv.visit(version, ACC_PUBLIC, AsmUtils.toAsmCls(enhancedClassName), fieldGetterTypeString1 + "<" + entityTypeString + ">;", name, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {

					if(name.equals(GETTER_METHOD_NAME)) {// get

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						//获取this.target
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);

						mv.visitMethodInsn(INVOKEVIRTUAL,
								AsmUtils.toAsmCls(clazz.getName()), getMethod.getName(),
								mt.toString());

						// 处理返回值类型 到 Object类型
						Type rt = Type.getReturnType(getMethod);
						AsmUtils.withBoxingType(mv, rt);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
						return mv;

					} else if(name.equals(SET_TARGET_METHOD_NAME)) {// setTarget

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						//调用this.target = object;
						mv.visitVarInsn(ALOAD, 0);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitTypeInsn(Opcodes.CHECKCAST, AsmUtils.toAsmCls(clazz.getName()));

						mv.visitFieldInsn(Opcodes.PUTFIELD,
								AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);
						mv.visitInsn(RETURN);
						mv.visitMaxs(2, 2);
						mv.visitEnd();
						return mv;

					} else if(name.equals(GET_NAME_METHOD_NAME)) {//getName
						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						mv.visitLdcInsn(field.getName());
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
					}


					return null;
				}

				@Override
				public void visitEnd() {

					//添加this.target属性
					writer.visitField(Opcodes.ACC_PROTECTED, REAL_OBJECT, entityTypeString, null, null);

					// 调用originalClassName的<init>方法，否则class不能实例化
					MethodVisitor mvInit = writer.visitMethod(ACC_PUBLIC, INIT, "()V",
							null, null);
					mvInit.visitVarInsn(ALOAD, 0);
					mvInit.visitMethodInsn(INVOKESPECIAL, fieldGetterClassName, INIT, "()V");
					mvInit.visitInsn(RETURN);
					mvInit.visitMaxs(1, 1);
					mvInit.visitEnd();

				}

			};

			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();

			AsmUtils.writeClazz(enhancedClassName, byteCodes);
			//load class
			enhancedClass = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);
		}

		try {
			return (ValueGetter<T>) enhancedClass.newInstance();
		} catch (Exception e) {
			logger.error("无法创建代理类对象:" + enhancedClassName);
			throw e;
		}

	}


	/**
	 * 创建属性设值器
	 * @param clazz 类
	 * @param field 属性
	 * @return ValueSetter<T>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> ValueSetter<T> createFieldSetter(final Class<T> clazz, final Field field) throws Exception {

		Class<T> enhancedClass = null;
		//代理类名
		final String enhancedClassName = AbstractFieldSetter.class.getName()
				+ SUFIX1 + SPLITER + clazz.getSimpleName() + SPLITER
				+ id.incrementAndGet();
		try {
			enhancedClass = (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(AbstractFieldSetter.class.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}


			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
			} catch (IntrospectionException e) {
				logger.error("无法获取set方法:" + clazz.getModifiers() + "(" + field.getName() + ")");
				e.printStackTrace();
			}
			//获取set方法
			final Method setMethod = propertyDescriptor.getWriteMethod();
			final Type[] mat = Type.getArgumentTypes(setMethod);
			final Class<?>[] mpt = setMethod.getParameterTypes();

			final Type mrt = Type.getType(setMethod);

			final String fieldSetterClassName = AsmUtils.toAsmCls(AbstractFieldSetter.class.getName());
			final String entityTypeString = Type.getDescriptor(clazz);
			final String fieldSetterTypeString = Type.getDescriptor(AbstractFieldSetter.class);
			final String fieldSetterTypeString1 = fieldSetterTypeString.substring(0, fieldSetterTypeString.length() - 1);

			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

				@Override
				public void visit(int version, int access, String name,
								  String signature, String superName, String[] interfaces) {
					cv.visit(version, ACC_PUBLIC, AsmUtils.toAsmCls(enhancedClassName), fieldSetterTypeString1 + "<" + entityTypeString + ">;", name, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name,
												 String desc, String signature, String[] exceptions) {

					if(name.equals(SETTER_METHOD_NAME)) {// set

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						//获取this.target
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);

						mv.visitVarInsn(ALOAD, 1);

						if(mpt[0].isPrimitive()) {
							// unBoxing
							AsmUtils.withUnBoxingType(mv, mat[0]);
						} else {
							mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(mpt[0].getName()));
						}

						mv.visitMethodInsn(INVOKEVIRTUAL,
								AsmUtils.toAsmCls(clazz.getName()), setMethod.getName(),
								mrt.toString());

						// 处理返回值类型 到 Object类型
						Type rt = Type.getReturnType(setMethod);
						AsmUtils.withBoxingType(mv, rt);
						if (rt.getSort() == Type.VOID) {
							mv.visitInsn(RETURN);
						} else {
							mv.visitInsn(ARETURN);
						}
						mv.visitMaxs(2, 2);
						mv.visitEnd();
						return mv;

					} else if(name.equals(SET_TARGET_METHOD_NAME)) {// setTarget

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						//调用this.target = object;
						mv.visitVarInsn(ALOAD, 0);
							mv.visitVarInsn(ALOAD, 1);
							mv.visitTypeInsn(Opcodes.CHECKCAST, AsmUtils.toAsmCls(clazz.getName()));

							mv.visitFieldInsn(Opcodes.PUTFIELD,
								AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);
						mv.visitInsn(RETURN);
						mv.visitMaxs(2, 2);
						mv.visitEnd();
						return mv;

					} else if(name.equals(GET_NAME_METHOD_NAME)) {//getName
						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						mv.visitLdcInsn(field.getName());
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
					}


					return null;
				}

				@Override
				public void visitEnd() {

					//添加this.target属性
					writer.visitField(Opcodes.ACC_PROTECTED, REAL_OBJECT, entityTypeString, null, null);

					// 调用originalClassName的<init>方法，否则class不能实例化
					MethodVisitor mvInit = writer.visitMethod(ACC_PUBLIC, INIT, "()V",
							null, null);
					mvInit.visitVarInsn(ALOAD, 0);
					mvInit.visitMethodInsn(INVOKESPECIAL, fieldSetterClassName, INIT, "()V");
					mvInit.visitInsn(RETURN);
					mvInit.visitMaxs(1, 1);
					mvInit.visitEnd();

				}

			};

			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();

			AsmUtils.writeClazz(enhancedClassName, byteCodes);
			//load class
			enhancedClass = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);
		}

		try {
			return (ValueSetter<T>) enhancedClass.newInstance();
		} catch (Exception e) {
			logger.error("无法创建代理类对象:" + enhancedClassName);
			throw e;
		}

	}



}
