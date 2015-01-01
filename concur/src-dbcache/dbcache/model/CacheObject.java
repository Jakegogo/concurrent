package dbcache.model;

import dbcache.conf.JsonConverter;
import dbcache.support.asm.ValueGetter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 实体缓存辅助类
 *
 * @author jake
 * @date 2014-7-31-下午8:18:03
 */
public class CacheObject<T extends IEntity<?>> {

	/**
	 * 缓存对象
	 */
	protected final T entity;

	/**
	 * 代理缓存对象
	 */
	protected final T proxyEntity;

	/**
	 * 主键id
	 */
	private final Serializable id;

	/**
	 * 实体类
	 */
	private final Class<T> clazz;

	/**
	 * 修改版本号
	 */
	private final AtomicLong editVersion = new AtomicLong(0L);

	/**
	 * 入库版本号
	 */
	private AtomicLong dbVersion = new AtomicLong(editVersion.get());
	
	/**
	 * 索引属性值获取器列表
	 */
	private List<ValueGetter<T>> indexList = new ArrayList<ValueGetter<T>>();

	/**
	 * Json 自动转换器
	 */
	private List<JsonConverter> jsonConverters = new ArrayList<JsonConverter>();
	
	/**
	 * 持久化状态
	 */
	private volatile PersistStatus persistStatus;

	/**
	 * 是否在更新处理中
	 */
	private AtomicBoolean updateProcessing = new AtomicBoolean(false);


	/**
	 * 默认构造方法
	 */
	protected CacheObject() {
		this.entity = null;
		this.id = null;
		this.clazz = null;
		this.proxyEntity = null;
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity) {
		this(entity, id, clazz, proxyEntity, null, null);
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param id
	 *            主键
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity, List<ValueGetter<T>> indexes, List<JsonConverter> jsonConverters) {
		this.entity = entity;
		this.id = id;
		this.clazz = clazz;
		this.proxyEntity = proxyEntity;
		this.persistStatus = PersistStatus.TRANSIENT;
		this.indexList = indexes;
		this.jsonConverters = jsonConverters;
	}

	/**
	 * 初始化
	 */
	public void doInit() {
		// 调用初始化
		if(entity instanceof EntityInitializer){
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doAfterLoad();
		}

		// 初始化json自动转换属性
		if(!getJsonConverters().isEmpty()) {
			for(JsonConverter jsonConverter : getJsonConverters()) {
				jsonConverter.doConvert();
			}
		}
	}

	/**
	 * 持久化之前的操作
	 */
	public void doBeforePersist() {
		// 持久化前操作
		if(entity instanceof EntityInitializer){
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doBeforePersist();
		}
		// json持久化
		if(!getJsonConverters().isEmpty()) {
			for(JsonConverter jsonConverter : getJsonConverters()) {
				jsonConverter.doPersist();
			}
		}
	}

	/**
	 * 比较并更新入库版本号
	 * @param dbVersion 已入库的版本号
	 * @param editVersion 编辑的版本号
	 * @return
	 */
	public boolean compareAndUpdateDbSync(long dbVersion, long editVersion) {
		return this.dbVersion.compareAndSet(dbVersion, editVersion);
	}

	/**
	 * 更新修改版本号
	 */
	public long increseEditVersion() {
		return this.editVersion.incrementAndGet();
	}

	public T getEntity() {
		return entity;
	}

	public T getProxyEntity() {
		return proxyEntity;
	}

	public Serializable getId() {
		return id;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public long getEditVersion() {
		return editVersion.get();
	}

	public long getDbVersion() {
		return dbVersion.get();
	}

	public PersistStatus getPersistStatus() {
		return persistStatus;
	}

	public void setPersistStatus(PersistStatus persistStatus) {
		this.persistStatus = persistStatus;
	}

	public List<ValueGetter<T>> getIndexList() {
		return indexList;
	}

	public boolean isUpdateProcessing() {
		return this.updateProcessing.get();
	}

	public void setUpdateProcessing(boolean processing) {
		this.updateProcessing.compareAndSet(!processing, processing);
	}

	public List<JsonConverter> getJsonConverters() {
		return jsonConverters;
	}

}