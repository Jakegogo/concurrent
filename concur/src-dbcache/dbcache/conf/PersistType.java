package dbcache.conf;

import dbcache.service.impl.DelayDbPersistService;
import dbcache.service.impl.InTimeDbPersistService;

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
	DELAY(DelayDbPersistService.class);


	/** 持久化类 */
	private Class<?> dbPersistServiceClass;


	PersistType(Class<?> dbPersistServiceClass) {
		this.dbPersistServiceClass = dbPersistServiceClass;
	}

	public Class<?> getDbPersistServiceClass() {
		return dbPersistServiceClass;
	}

}
