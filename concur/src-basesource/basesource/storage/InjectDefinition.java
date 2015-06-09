package basesource.storage;

import java.lang.reflect.Field;

import org.springframework.context.ApplicationContext;

import utils.StringUtils;
import basesource.anno.InjectBean;


/**
 * 注入信息定义
 * 
 * @author frank
 */
public class InjectDefinition {

	/** 被注入的属性 */
	private final Field field;
	/** 注入配置 */
	private final InjectBean inject;
	/** 注入类型 */
	private final InjectType type;
	
	public InjectDefinition(Field field) {
		if (field == null) {
			throw new IllegalArgumentException("被注入属性域不能为null");
		}
		if (!field.isAnnotationPresent(InjectBean.class)) {
			throw new IllegalArgumentException("被注入属性域" + field.getName() + "的注释配置缺失");
		}
		field.setAccessible(true);
		
		this.field = field;
		this.inject = field.getAnnotation(InjectBean.class);
		if (StringUtils.isEmpty(this.inject.value())) {
			this.type = InjectType.CLASS;
		} else {
			this.type = InjectType.NAME;
		}
	}
	
	/**
	 * 获取注入值
	 * @param applicationContext
	 * @return
	 */
	public Object getValue(ApplicationContext applicationContext) {
		if (InjectType.NAME.equals(type)) {
			return applicationContext.getBean(inject.value());
		} else {
			return applicationContext.getBean(field.getType());
		}
	}

	// Getter and Setter ...

	public InjectType getType() {
		return type;
	}

	public Field getField() {
		return field;
	}

	public InjectBean getInject() {
		return inject;
	}

}
