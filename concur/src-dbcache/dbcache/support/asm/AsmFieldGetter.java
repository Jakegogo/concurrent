package dbcache.support.asm;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dbcache.utils.AsmUtils;

/**
 * ASM属性获取器
 * for version <= jdk 1.6
 * @author Jake
 * @date 2014年11月1日上午1:12:54
 */
public class AsmFieldGetter<T> extends AbstractFieldGetter<T> implements Opcodes {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(AsmFieldGetter.class);

	/** 获取的方法名 */
	public static final String GETTER_METHOD_NAME = "get";

	/** 构造方法名常量 */
	private static final String INIT = "<init>";

	/** 代理类类名 */
	public static final String SUFIX = "$EnhancedByAsmFieldGetter";

	/** 分隔符 */
	public static final String SPLITER = "_";

	/** 序号生成器 */
	private static AtomicLong id = new AtomicLong(0);

	/**
	 * 字节码类加载器
	 */
	public static BytecodeLoader classLoader = new BytecodeLoader();


	private Class<T> clazz;


	private Field field;

	/** 属性获取的asm代理对象 */
	private ValueGetter<T> fieldGetter;

	/** 值的名称 */
	private String name;

	public static class BytecodeLoader extends ClassLoader {
		public Class<?> defineClass(String className, byte[] byteCodes) {
			return super.defineClass(className, byteCodes, 0, byteCodes.length);
		}
	}

	/**
	 * 默认构造方法
	 */
	protected AsmFieldGetter() {

	}

	/**
	 * 构造方法
	 * @param clazz 类
	 * @param field 属性
	 */
	protected AsmFieldGetter(Class<T> clazz, Field field) {
		this.clazz = clazz;
		this.field = field;
		this.name = field.getName();
		this.init(clazz, field);
	}


	/**
	 * 获取实例
	 * @param clazz 类
	 * @param field 属性
	 * @return
	 */
	public static <T> AsmFieldGetter<T> valueOf(Class<T> clazz, Field field) {
		return new AsmFieldGetter<T>(clazz, field);
	}


	//初始化
	@SuppressWarnings("unchecked")
	private void init(final Class<T> clazz, final Field field) {

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
					if(name.equals(GETTER_METHOD_NAME)) {
						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						//调用object.getField
						mv.visitVarInsn(ALOAD, 1);
						mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(clazz.getName()));
						mv.visitMethodInsn(INVOKEVIRTUAL,
								AsmUtils.toAsmCls(clazz.getName()), getMethod.getName(),
								mt.toString());

						// 处理返回值类型 到 Object类型
						Type rt = Type.getReturnType(getMethod);
						AsmUtils.withBoxingType(mv, rt);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(0, 0);
						mv.visitEnd();
						return mv;
					}
					return null;
				}

				@Override
				public void visitEnd() {
					// 调用originalClassName的<init>方法，否则class不能实例化
					MethodVisitor mvInit = writer.visitMethod(ACC_PUBLIC, INIT, "()V",
							null, null);
					mvInit.visitVarInsn(ALOAD, 0);
					mvInit.visitMethodInsn(INVOKESPECIAL, fieldGetterClassName, INIT, "()V");
					mvInit.visitInsn(RETURN);
					mvInit.visitMaxs(0, 0);
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
			this.fieldGetter = (ValueGetter<T>) enhancedClass.newInstance();
		} catch (Exception e) {
			logger.error("无法创建代理类对象:" + enhancedClassName);
			e.printStackTrace();
		}


	}


	/**
	 * 获取值
	 * @return
	 */
	@Override
	public Object get(T object) {
		return fieldGetter.get(object);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Field getField() {
		return field;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
