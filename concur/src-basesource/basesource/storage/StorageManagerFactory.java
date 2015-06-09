package basesource.storage;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * 资源管理器工厂
 * @author frank
 */
public class StorageManagerFactory implements FactoryBean<StorageManager>, ApplicationContextAware {
	
	/** 资源定义列表 */
	private List<ResourceDefinition> definitions;
	
	public void setDefinitions(List<ResourceDefinition> definitions) {
		this.definitions = definitions;
	}
	
	private StorageManager storageManager;
	
	@PostConstruct
	protected void initialize() {
		storageManager = this.applicationContext.getAutowireCapableBeanFactory().createBean(StorageManager.class);
		for (ResourceDefinition definition : definitions) {
			storageManager.initialize(definition);
		}
	}

	@Override
	public StorageManager getObject() throws Exception {
		return storageManager;
	}
	
	// 实现接口的方法

	@Override
	public Class<StorageManager> getObjectType() {
		return StorageManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
