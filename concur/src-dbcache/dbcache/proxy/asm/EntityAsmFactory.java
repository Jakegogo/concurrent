package dbcache.proxy.asm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.annotation.ThreadSafe;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import dbcache.proxy.AbstractAsmMethodAspect;
import dbcache.support.asm.AsmClassLoader;

/**
 * asm代理工厂 <br/>
 * 带缓存
 *
 * @author Jake
 * @date 2014年9月6日上午12:28:13
 */
@ThreadSafe
public class EntityAsmFactory {

	/**
	 * 代理类缓存
	 */
	public static ConcurrentHashMap<Class<?>, Class<?>> ENHANCED_CLASS_CACHE = new ConcurrentHashMap<Class<?>, Class<?>>();


	/** 代理类类名 */
	public static final String SUFIX = "$EnhancedByAsm";

	/**
	 * 字节码类加载器
	 */
	public static AsmClassLoader classLoader = new AsmClassLoader();


	/**
	 *
	 * <p>
	 * 返回代理类
	 * </p>
	 * 使用默认的AbstractMethodAspect
	 *
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getEntityEnhancedClass(Class<T> clazz) {
		// 从缓存这获取
		if (ENHANCED_CLASS_CACHE.containsKey(clazz)) {
			return (Class<T>) ENHANCED_CLASS_CACHE.get(clazz);
		}

		String enhancedClassName = clazz.getName() + SUFIX;
		try {
			return (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(clazz.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new EntityClassAdapter(enhancedClassName, clazz,
					writer);
			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();
//			writeClazz(enhancedClassName, byteCodes);
			Class<T> result = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);

			// 将代理类存入缓存
			ENHANCED_CLASS_CACHE.putIfAbsent(clazz, result);

			return (Class<T>) ENHANCED_CLASS_CACHE.get(clazz);
		}
	}

	/**
	 *
	 * <p>
	 * 返回代理类
	 * </p>
	 *
	 * @param <T>
	 * @param clazz
	 *            Class<T>
	 * @param methodAspect
	 *            AbstractMethodAspect
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getEntityEnhancedClass(Class<T> clazz,
			AbstractAsmMethodAspect methodAspect) {
		// 从缓存这获取
		if (ENHANCED_CLASS_CACHE.containsKey(clazz)) {
			return (Class<T>) ENHANCED_CLASS_CACHE.get(clazz);
		}

		String enhancedClassName = clazz.getName() + SUFIX;
		try {
			return (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(clazz.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

			// 初始化实体类的方法切面信息
			methodAspect.initClass(clazz, enhancedClassName);
			ClassVisitor visitor = new EntityClassAdapter(enhancedClassName, clazz,
					writer, methodAspect);
			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();
//			writeClazz(enhancedClassName, byteCodes);
			Class<T> result = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);

			// 将代理类存入缓存
			ENHANCED_CLASS_CACHE.putIfAbsent(clazz, result);

			return (Class<T>) ENHANCED_CLASS_CACHE.get(clazz);
		}
	}

	/**
	 *
	 * <p>
	 * 把java字节码写入class文件
	 * </p>
	 *
	 * @param <T>
	 * @param name
	 * @param data
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static <T> void writeClazz(String name, byte[] data) {
		try {
			File file = new File("C:\\" + name + ".class");
			FileOutputStream fout = new FileOutputStream(file);

			fout.write(data);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
