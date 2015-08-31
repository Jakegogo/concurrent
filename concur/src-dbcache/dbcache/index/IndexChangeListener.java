package dbcache.index;

import dbcache.IEntity;

import java.io.Serializable;

/**
 * 索引值变化监听器
 * Created by Jake on 2015/7/19.
 */
public interface IndexChangeListener<T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> {

    /**
     * 索引值创建时回调
     * @param name 索引名称
     * @param value 索引值
     * @param key 实体KEY
     */
    void onIndexCreate(String name, Object value, PK key);

    /**
     * 索引值被移除时回调
     * @param name 索引名称
     * @param value 索引值
     * @param key 实体KEY
     */
    void onIndexRemove(String name, Object value, PK key);

    /**
     * 索引值修改时回调
     * @param name 索引名称
     * @param oldValue 索引旧值
     * @param newValue 索引新值
     * @param key 实体KEY
     */
    void onIndexChange(String name, Object oldValue, Object newValue, PK key);

}
