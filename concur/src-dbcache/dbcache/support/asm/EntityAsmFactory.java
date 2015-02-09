package dbcache.support.asm;

import dbcache.utils.AsmUtils;

import org.apache.http.annotation.ThreadSafe;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

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
	public static ConcurrentHashMap<Class<?>, EnhancedClassInfo<?>> ENHANCED_CLASS_CACHE = new ConcurrentHashMap<Class<?>, EnhancedClassInfo<?>>();

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
			EnhancedClassInfo<T> classInfo = (EnhancedClassInfo<T>) ENHANCED_CLASS_CACHE.get(clazz);
			if (classInfo != null) {
				return classInfo.getProxyClass();
			}
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
			ClassVisitor visitor = new EntityClassProxyAdapter(enhancedClassName, clazz,
					writer);
			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();
			AsmUtils.writeClazz(enhancedClassName, byteCodes);
			Class<T> result = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);

			EnhancedClassInfo classInfo = new EnhancedClassInfo();
			classInfo.setProxyClass(clazz);
			
			// 将代理类存入缓存
			ENHANCED_CLASS_CACHE.putIfAbsent(clazz, classInfo);

			return getEntityEnhancedClass(clazz);
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
	public static <T> EnhancedClassInfo<T> getEntityEnhancedClassInfo(Class<T> clazz,
			AbstractAsmMethodProxyAspect methodAspect) {
		// 从缓存这获取
		if (ENHANCED_CLASS_CACHE.containsKey(clazz)) {
			return (EnhancedClassInfo<T>) ENHANCED_CLASS_CACHE.get(clazz);
		}

		String enhancedClassName = clazz.getName() + SUFIX;

		ClassReader reader = null;
		try {
			reader = new ClassReader(clazz.getName());
		} catch (IOException ioexception) {
			throw new RuntimeException(ioexception);
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		// 初始化实体类的方法切面信息
		methodAspect.initClassMetaInfo(clazz, enhancedClassName);

		ClassVisitor visitor = new EntityClassProxyAdapter(enhancedClassName, clazz,
				writer, methodAspect);

		ConstructorBuilder constructorBuilder = new ConstructorBuilder(writer, clazz, enhancedClassName);
		
		// 增加原实体类型的属性(真实类)
		constructorBuilder.appendField(constructorBuilder.getOriginalClass(), EntityClassProxyAdapter.REAL_OBJECT);

		// 添加切面处理对象构造方法,用真实类对象作为参数
		constructorBuilder.appendParameter(clazz, new ConstructorBuilder.ParameterInit () {

			@Override
			/**
			 * @see dbcache.support.asm.ConstructorBuilder#getProxyEntity(java.lang.Class<T>, T, dbcache.service.DbIndexService, java.util.concurrent.atomic.AtomicIntegerArray)
			 */
			public int parameterIndexOfgetProxyEntity() {
				return 0;
			}

			@Override
			public void onConstruct(ClassWriter classWriter, MethodVisitor mvInit, Class<?> originalClass, String enhancedClassName, int localIndex) {
				mvInit.visitVarInsn(Opcodes.ALOAD, 0);
				mvInit.visitVarInsn(Opcodes.ALOAD, localIndex);

				mvInit.visitFieldInsn(Opcodes.PUTFIELD,
						AsmUtils.toAsmCls(enhancedClassName), EntityClassProxyAdapter.REAL_OBJECT,
						Type.getDescriptor(originalClass));
			}
		});

		// 调用methodAspect初始化构造方法
		methodAspect.doInitClass(constructorBuilder);

		reader.accept(visitor, 0);
		byte[] byteCodes = writer.toByteArray();
		AsmUtils.writeClazz(enhancedClassName, byteCodes);
		Class<T> proxyClass = (Class<T>) classLoader.defineClass(
				enhancedClassName, byteCodes);

		EnhancedClassInfo classInfo = new EnhancedClassInfo();
		classInfo.setProxyClass(proxyClass);
		classInfo.setConstructorBuilder(constructorBuilder);

		// 将代理类存入缓存
		ENHANCED_CLASS_CACHE.putIfAbsent(clazz, classInfo);

		return (EnhancedClassInfo<T>) ENHANCED_CLASS_CACHE.get(clazz);

	}
	

}
