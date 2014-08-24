package dbcache.model;

import java.io.Serializable;
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
	private volatile AtomicLong dbVersion = new AtomicLong(editVersion.get());

	/**
	 * 实体更新状态
	 */
	private volatile UpdateStatus updateStatus = UpdateStatus.PERSIST;

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
	public CacheObject(T entity, Serializable id, Class<T> clazz) {
		this(entity, id, clazz, UpdateStatus.PERSIST);
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
	public CacheObject(T entity, Serializable id, Class<T> clazz,
			UpdateStatus updateStatus) {
		this.entity = entity;
		this.id = id;
		this.clazz = clazz;
		this.updateStatus = updateStatus;
	}

	
	/**
	 * 比较并更新入库版本号
	 * 
	 * @param updateAction
	 *            更新操作
	 * @return
	 */
	public boolean compareDbSync(UpdateAction updateAction) {
		return this.dbVersion.get() == updateAction.getDbVersion();
	}

	/**
	 * 比较并更新入库版本号
	 * 
	 * @param updateAction
	 *            更新操作
	 * @return
	 */
	public boolean compareAndUpdateDbSync(UpdateAction updateAction) {
		return this.dbVersion.compareAndSet(updateAction.getDbVersion(),
				updateAction.getEditVersion());
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

}