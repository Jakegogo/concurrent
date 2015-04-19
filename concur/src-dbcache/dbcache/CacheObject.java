package dbcache;

import dbcache.conf.CacheConfig;
import dbcache.persist.PersistStatus;
import utils.thread.SimpleLinkingRunnable;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 单个实体缓存数据结构
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
	 * 修改过的属性
	 */
	private AtomicIntegerArray modifiedFields;

	// for persist
	
	/**
	 * 持久化状态
	 */
	private volatile PersistStatus persistStatus;

	/**
	 * 是否在更新处理中
	 */
	private volatile boolean updateProcessing = false;

	//-----执行链-----
	/**
	 * 上一次执行的线程
	 */
	private volatile AtomicReference<SimpleLinkingRunnable> lastLinkingRunnable;
	
	/**
	 * 默认构造方法
	 */
	protected CacheObject() {
		this.entity = null;
		this.proxyEntity = null;
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Class<T> clazz, T proxyEntity) {
		this(entity, clazz, proxyEntity, null);
	}

	/**
	 * 构造方法
	 *
	 * @param entity
	 *            实体
	 * @param clazz
	 *            类型
	 */
	public CacheObject(T entity, Class<T> clazz, T proxyEntity, AtomicIntegerArray modifiedFields) {
		this.entity = entity;
		this.proxyEntity = proxyEntity;
		this.persistStatus = PersistStatus.TRANSIENT;
		this.modifiedFields = modifiedFields;
	}

	/**
	 * 初始化
	 */
	public void doInit(CacheConfig<T> cacheConfig) {
		// 加载回调
		this.doAfterLoad();
	}
	
	/**
	 * 调用加载回调
	 */
	public void doAfterLoad() {
		// 调用初始化
		if (entity instanceof EntityInitializer){
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doAfterLoad();
		}
	}

	/**
	 * 持久化之前的操作
	 */
	public void doBeforePersist(CacheConfig<T> cacheConfig) {
		// 持久化前操作
		if (entity instanceof EntityInitializer) {
			EntityInitializer entityInitializer = (EntityInitializer) entity;
			entityInitializer.doBeforePersist();
		}
	}
	

	@Override
	public int hashCode() {
		if (this.entity.getId() == null) {
			return this.entity.getClass().hashCode() * 31;
		}
		return this.entity.getClass().hashCode() * 31 + this.entity.getId().hashCode();
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CacheObject)) {
			return false;
		}
		
		CacheObject<T> target = (CacheObject<T>) obj;
		
		return new EqualsBuilder()
				.append(this.entity.getClass(), target.entity.getClass())
				.append(this.entity.getId(), target.entity.getId()).isEquals();
	}

	public T getEntity() {
		return entity;
	}

	public T getProxyEntity() {
		return proxyEntity;
	}

	public PersistStatus getPersistStatus() {
		return persistStatus;
	}

	public void setPersistStatus(PersistStatus persistStatus) {
		this.persistStatus = persistStatus;
	}

	public boolean isUpdateProcessing() {
		return this.updateProcessing;
	}

	public void setUpdateProcessing(boolean updateProcessing) {
		this.updateProcessing = updateProcessing;
	}

	public AtomicReference<SimpleLinkingRunnable> getLastLinkingRunnable() {
		return lastLinkingRunnable;
	}

	public void setLastLinkingRunnable(
			AtomicReference<SimpleLinkingRunnable> lastLinkingRunnable) {
		this.lastLinkingRunnable = lastLinkingRunnable;
	}

	public AtomicIntegerArray getModifiedFields() {
		return modifiedFields;
	}
}