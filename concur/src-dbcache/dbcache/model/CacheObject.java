package dbcache.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import dbcache.support.asm.ValueGetter;


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
	 * 索引信息
	 * 索引名 - 属性
	 */
	private Map<String, ValueGetter<T>> indexes = new HashMap<String, ValueGetter<T>>();

	/**
	 * 持久化状态
	 */
	private volatile PersistStatus persistStatus;

	/**
	 * 是否在更新处理中
	 */
	private volatile boolean updateProcessing = false;


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
		this(entity, id, clazz, proxyEntity, null);
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
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity, Map<String, ValueGetter<T>> indexes) {
		this.entity = entity;
		this.id = id;
		this.clazz = clazz;
		this.proxyEntity = proxyEntity;
		this.indexes = indexes;
		this.persistStatus = PersistStatus.TRANSIENT;
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

	public Map<String, ValueGetter<T>> getIndexes() {
		return indexes;
	}


	public PersistStatus getPersistStatus() {
		return persistStatus;
	}

	public void setPersistStatus(PersistStatus persistStatus) {
		this.persistStatus = persistStatus;
	}

	public boolean isUpdateProcessing() {
		return updateProcessing;
	}

	public void setUpdateProcessing(boolean updateProcessing) {
		this.updateProcessing = updateProcessing;
	}
}