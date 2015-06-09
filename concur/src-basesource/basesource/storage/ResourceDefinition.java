package basesource.storage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import utils.StringUtils;
import utils.reflect.ReflectionUtility;
import basesource.anno.InjectBean;
import basesource.anno.Resource;


/**
 * 资源定义信息对象
 * 
 * @author frank
 */
public class ResourceDefinition {
	
	public final static String FILE_SPLIT = ".";
	public final static String FILE_PATH = File.separator;

	/** 注入属性域过滤器 */
	private final static FieldFilter INJECT_FILTER = new FieldFilter() {
		@Override
		public boolean matches(Field field) {
			if (field.isAnnotationPresent(InjectBean.class)) {
				return true;
			}
			return false;
		}
	};

	/** 资源类 */
	private final Class<?> clz;
	/** 资源路径 */
	private final String location;
	/** 资源格式 */
	private final String format;
	/** 资源的注入信息 */
	private final Set<InjectDefinition> injects = new HashSet<InjectDefinition>();
	
	/** 构造方法 */
	public ResourceDefinition(Class<?> clz, FormatDefinition format) {
		this.clz = clz;
		this.format = format.getType();
		Resource anno = clz.getAnnotation(Resource.class);
		StringBuilder builder = new StringBuilder();
		builder.append(format.getLocation()).append(FILE_PATH);
		if (!StringUtils.isBlank(anno.value())) {
			String dir = anno.value();
			int start = 0;
			int end = dir.length();
			if (dir.startsWith(FILE_PATH)) {
				start++;
			}
			if (dir.endsWith(FILE_PATH)) {
				end--;
			}
			builder.append(dir.substring(start, end)).append(FILE_PATH);
		}
		builder.append(clz.getSimpleName()).append(FILE_SPLIT).append(format.getSuffix());
		this.location = builder.toString();
		ReflectionUtility.doWithDeclaredFields(clz, new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				InjectDefinition definition = new InjectDefinition(field);
				injects.add(definition);
			}
		}, INJECT_FILTER);
	}
	
	/**
	 * 获取静态属性注入定义
	 * @return
	 */
	public Set<InjectDefinition> getStaticInjects() {
		HashSet<InjectDefinition> result = new HashSet<InjectDefinition>();
		for (InjectDefinition definition : this.injects) {
			Field field = definition.getField();
			if (Modifier.isStatic(field.getModifiers())) {
				result.add(definition);
			}
		}
		return result;
	}
	
	/**
	 * 获取非静态属性注入定义
	 * @return
	 */
	public Set<InjectDefinition> getInjects() {
		HashSet<InjectDefinition> result = new HashSet<InjectDefinition>();
		for (InjectDefinition definition : this.injects) {
			Field field = definition.getField();
			if (!Modifier.isStatic(field.getModifiers())) {
				result.add(definition);
			}
		}
		return result;
	}
	
	/**
	 * 资源是否需要校验
	 * @return
	 */
	public boolean isNeedValidate() {
		// TODO
//		if (Validate.class.isAssignableFrom(clz)) {
//			return true;
//		}
		return false;
	}
	
	// Getter and Setter ...
	
	public Class<?> getClz() {
		return clz;
	}

	public String getLocation() {
		return location;
	}

	public String getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
