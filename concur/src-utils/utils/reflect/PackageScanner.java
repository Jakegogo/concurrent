/**
 * 
 */
package utils.reflect;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;


/**
 * 包扫描器
 * @author fansth
 *
 */
public class PackageScanner {
	
	private static final Logger logger = LoggerFactory.getLogger(PackageScanner.class);
	
	private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
	//资源的格式  ant匹配符号格式
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	
	/**
	 * 扫描指定包中所有的类(包括子类)
	 * @param packageNames 包名支持权限定包名和ant风格匹配符(同spring)
	 * @return 
	 */
	public static Collection<Class<?>> scanPackages(String... packageNames){
		Collection<Class<?>> clazzCollection = new HashSet<Class<?>>();

		for (String packageName : packageNames) {
			
			try {
				// 搜索资源
				String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
						+ resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
				Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
				
				for(Resource resource : resources){
					
					String className = "";
					
					try {
						if (!resource.isReadable()) {
							continue;
						}
						// 判断是否静态资源
						MetadataReader metaReader = metadataReaderFactory.getMetadataReader(resource);
						className = metaReader.getClassMetadata().getClassName();
						
						Class<?> clazz = Class.forName(className);
						clazzCollection.add(clazz);
						
					} catch (ClassNotFoundException e) {
						logger.error("类 {} 不存在!", className);
						throw new RuntimeException(e);
					}
					
				}
				
			} catch (IOException e) {
				logger.error("扫描包 {} 出错!", packageName);
				throw new RuntimeException(e);
			}
		}
		
		return clazzCollection;
		
	}
	
	
	/**
	 * 扫描指定包中所有的类(包括子类)
	 * @param classLoader 指定ClassLoader
	 * @param packageNames 包名支持权限定包名和ant风格匹配符(同spring)
	 * @return 
	 */
	public static Collection<Class<?>> filterByAnnotation(ClassLoader classLoader, String... packageNames){
		Collection<Class<?>> clazzCollection = new HashSet<Class<?>>();

		for (String packageName : packageNames) {
			
			try {
				// 搜索资源
				String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
						+ resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
				Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
				
				for(Resource resource : resources){
					
					String className = "";
					
					try {
						if (!resource.isReadable()) {
							continue;
						}
						// 判断是否静态资源
						MetadataReader metaReader = metadataReaderFactory.getMetadataReader(resource);
						className = metaReader.getClassMetadata().getClassName();
						
						Class<?> clazz = classLoader.loadClass(className);
						clazzCollection.add(clazz);
						
					} catch (ClassNotFoundException e) {
						logger.error("类 {} 不存在!", className);
						throw new RuntimeException(e);
					}
					
				}
				
			} catch (IOException e) {
				logger.error("扫描包 {} 出错!", packageName);
				throw new RuntimeException(e);
			}
		}
		
		return clazzCollection;
		
	}
	
	
	/**
	 * 将包名转换成目录名(com.my9yu-->com/my9yu)
	 * @param basePackage 包名
	 * @return
	 */
	private static String resolveBasePackage(String basePackage) {
		String placeHolderReplace = SystemPropertyUtils.resolvePlaceholders(basePackage);//${classpath}替换掉placeholder 引用的变量值
		return ClassUtils.convertClassNameToResourcePath(placeHolderReplace);
	}
	
	
}
