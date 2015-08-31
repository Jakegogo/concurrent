package dbcache.conf;

import dbcache.persist.service.impl.DelayBatchDbPersistService;
import dbcache.persist.service.impl.DelayDbPersistService;
import dbcache.persist.service.impl.InTimeDbPersistService;

/**
 * 持久化处理类型
 * @author Jake
 * @date 2014年9月14日下午5:14:25
 */
public enum PersistType {

	/**
	 * 即时入库
	 */
	INTIME(InTimeDbPersistService.class),


	/**
	 * 延时入库
	 */
	DELAY(DelayDbPersistService.class, "delayDbPersistService"),
	
	
	/**
	 * 延时批量入库
	 * <br/>此方式不支持DynamicUpdate
	 * <br/>不能支持到Hibernate入库方式
	 */
	DELAY_BATCH(DelayBatchDbPersistService.class, "delayBatchDbPersistService");


	/** 持久化类 */
	private final Class<?> dbPersistServiceClass;

	/** bean名称 */
	private String beanName;

	PersistType(Class<?> dbPersistServiceClass) {
		this.dbPersistServiceClass = dbPersistServiceClass;
	}
	
	PersistType(Class<?> dbPersistServiceClass, String beanName) {
		this.dbPersistServiceClass = dbPersistServiceClass;
		this.beanName = beanName;
	}

	public Class<?> getDbPersistServiceClass() {
		return dbPersistServiceClass;
	}

	public String getBeanName() {
		return beanName;
	}

}
