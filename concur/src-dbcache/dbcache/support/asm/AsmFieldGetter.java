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

/**
 * ASM属性获取器
 * @author Jake
 * @date 2014年11月1日上午1:12:54
 */
public class AsmFieldGetter {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(AsmFieldGetter.class);

	/** 代理类类名 */
	public static final String SUFIX = "$EnhancedByAsm";

	/** 分隔符 */
	public static final String SPLITER = "_";

	/** 序号生成器 */
	private static AtomicLong id = new AtomicLong(0);

	/**
	 * 字节码类加载器
	 */
	public static AsmClassLoader classLoader = new AsmClassLoader();

	private Class<?> clazz;

	private Field field;

	/** 属性获取的asm代理对象 */
	private AsmFieldGetter fieldGetter;

	/**
	 * 构造方法
	 * @param clazz 类
	 * @param field 属性
	 */
	protected AsmFieldGetter(Class<?> clazz, Field field) {
		this.clazz = clazz;
		this.field = field;
		this.init(clazz, field);
	}


	/**
	 * 获取实例
	 * @param clazz 类
	 * @param field 属性
	 * @return
	 */
	public static AsmFieldGetter valueOf(Class<?> clazz, Field field) {
		return new AsmFieldGetter(clazz, field);
	}


	//初始化
	private void init(Class<?> clazz, Field field) {

		Class<?> enhancedClass = null;

		String enhancedClassName = clazz.getName() + SPLITER + id.incrementAndGet() + SPLITER + SUFIX;
		try {
			enhancedClass = classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(clazz.getName());
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
			Type mt = Type.getType(getMethod);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {
					if(name.equals(getMethod.getName())) {

					}
					return super.visitMethod(access, name, desc, signature, exceptions);
				}

			};
			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();
			enhancedClass = (Class<?>) classLoader.defineClass(
					enhancedClassName, byteCodes);
		}





	}


	/**
	 * 获取值
	 * @return
	 */
	public Object get(Object object) {
		return fieldGetter.get(object);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Field getField() {
		return field;
	}

}
