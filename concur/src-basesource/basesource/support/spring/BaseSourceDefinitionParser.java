package basesource.support.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 扫描基础数据定义
 * Created by Jake on 2015/6/20.
 */
@Component
public class BaseSourceDefinitionParser implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BaseSourceDefinitionParser.class);

    /** 默认资源匹配符 */
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /** 基础数据定义扫描路径 */
    @Autowired
    @Qualifier("BASE_SOURCE_SCAN_PATTERN")
    protected String BASE_SOURCE_SCAN_PATTERN;

    /** 基础数据文件扫描路径 */
    @Autowired
    @Qualifier("BASE_SOURCE_SCAN_PATTERN")
    protected String BASE_SOURCE_FILE_PATH;

    /** 资源搜索分析器，由它来负责检索EAO接口 */
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    /** 类的元数据读取器，由它来负责读取类上的注释信息 */
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.initializeBasesourceDefinition();
    }

    // 初始化基础数据定义
    private void initializeBasesourceDefinition() {
        String[] names = getResources(BASE_SOURCE_SCAN_PATTERN);
        for (String resource : names) {
            Class<?> clz = null;
            try {
                clz = Class.forName(resource);
            } catch (ClassNotFoundException e) {
                FormattingTuple message = MessageFormatter.format("无法获取的资源类[{}]", resource);
                logger.error(message.getMessage());
                throw new RuntimeException(message.getMessage(), e);
            }


//            FormatDefinition FormatDefinition = new FormatDefinition(clz, );

        }

    }


    /**
     * 获取指定包下的静态资源对象
     * @param packageName 包名
     * @return
     */
    private String[] getResources(String packageName) {
        try {
            // 搜索资源
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            // 提取资源
            Set<String> result = new HashSet<String>();
            String name = basesource.anno.Resource.class.getName();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                // 判断是否静态资源
                MetadataReader metaReader = this.metadataReaderFactory.getMetadataReader(resource);
                AnnotationMetadata annoMeta = metaReader.getAnnotationMetadata();
                if (!annoMeta.hasAnnotation(name)) {
                    continue;
                }
                ClassMetadata clzMeta = metaReader.getClassMetadata();
                result.add(clzMeta.getClassName());
            }

            return result.toArray(new String[result.size()]);
        } catch (IOException e) {
            String message = "无法读取资源信息";
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }


    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

}
