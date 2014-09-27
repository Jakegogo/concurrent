package dbcache.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private final T entity;

	/**
	 * 代理缓存对象
	 */
	private final T proxyEntity;

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
	 * 实体更新状态
	 */
	private volatile UpdateStatus updateStatus = UpdateStatus.PERSIST;

	/**
	 * 索引键集合
	 */
	private Map<String, IndexKey> indexKeys = new HashMap<String, IndexKey>();

	/**
	 * 索引缓存列表
	 */
	private List<IndexObject<?>> indexObjects = new ArrayList<IndexObject<?>>();

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
		this(entity, id, clazz, proxyEntity, UpdateStatus.PERSIST);
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
	 * @param updateStatus
	 *            更新方式
	 */
	public CacheObject(T entity, Serializable id, Class<T> clazz, T proxyEntity,
			UpdateStatus updateStatus) {
		this.entity = entity;
		this.id = id;
		this.clazz = clazz;
		this.proxyEntity = proxyEntity;
		this.updateStatus = updateStatus;
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

	/**
	 * 设置状态
	 *
	 * @param newStatus
	 *            新状态
	 */
	public void setUpdateStatus(UpdateStatus updateStatus) {
		this.updateStatus = updateStatus;
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

	public UpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public Map<String, IndexKey> getIndexKeys() {
		return indexKeys;
	}

	public void setIndexKeys(Map<String, IndexKey> indexKeys) {
		this.indexKeys = indexKeys;
	}

	public List<IndexObject<?>> getIndexObjects() {
		return indexObjects;
	}

	public void setIndexObjects(List<IndexObject<?>> indexObjects) {
		this.indexObjects = indexObjects;
	}


}