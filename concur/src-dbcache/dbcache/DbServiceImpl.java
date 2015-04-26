package dbcache;

import dbcache.anno.ThreadSafe;
import dbcache.conf.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 通用DbCacheService实现
 * @see DbCacheServiceImpl
 * Created by Jake on 2015/4/26.
 */
@ThreadSafe
@Component
public class DbServiceImpl implements DbService {

    @Autowired
    private ConfigFactory configFactory;

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T get(Class<T> clazz, PK id) {
        return this.getDbCacheService(clazz).get(id);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> List<T> listById(Class<T> clazz, Collection<PK> idList) {
        return this.getDbCacheService(clazz).listById(idList);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> T submitCreate(T entity) {
        return this.getDbCacheService(getRealClass(entity)).submitCreate(entity);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitUpdate(T entity) {
        this.getDbCacheService(getRealClass(entity)).submitUpdate(entity);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitDelete(T entity) {
        this.getDbCacheService(getRealClass(entity)).submitDelete(entity);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> void submitDelete(Class<T> clazz, PK id) {
        this.getDbCacheService(clazz).submitDelete(id);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> List<T> listByIndex(Class<T> clazz, String indexName, Object indexValue) {
        return this.getDbCacheService(clazz).listByIndex(indexName, indexValue);
    }

    @Override
    public <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> Collection<PK> listIdByIndex(Class<T> clazz, String indexName, Object indexValue) {
        return this.getDbCacheService(clazz).listIdByIndex(indexName, indexValue);
    }

    // 获取DbCacheService
    private <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> DbCacheService<T, PK> getDbCacheService(Class<T> clazz) {
        if (clazz == null) {
            throw new DbCacheInitError("无法获取DbCacheService:null");
        }
        DbCacheService<T, PK> dbCacheService = configFactory.getDbCacheServiceBean(clazz);
        if (dbCacheService == null) {
            throw new DbCacheInitError("无法获取DbCacheService<" + clazz.getSimpleName() + ",?>");
        }
        return dbCacheService;
    }

    // 获取真实类
    private <T extends IEntity<PK>, PK extends Comparable<PK> & Serializable> Class<T> getRealClass(T entity) {
        if (entity instanceof EnhancedEntity) {
            IEntity<?> orignEntity = ((EnhancedEntity) entity).getEntity();
            if (orignEntity == null) {
                return null;
            }
            return (Class<T>) orignEntity.getClass();
        }
        return (Class<T>) entity.getClass();
    }

}
