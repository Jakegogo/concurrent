package dbcache.test;

import dbcache.DbCacheService;
import dbcache.EnhancedEntity;
import dbcache.cache.common.CacheLoader;
import dbcache.cache.common.CommonCache;
import dbcache.index.IndexChangeListener;
import dbcache.support.jdbc.JdbcSupport;
import dbcache.utils.CacheUtils;
import dbcache.utils.JdbcUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by Jake on 2015/8/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class TestIndexChangeListener implements IndexChangeListener<Entity, Long> {

    @Autowired
    private JdbcSupport jdbcSupport;

    @Autowired
    private DbCacheService<Entity, Long> dbCacheService;


    // 用户名 - ID 缓存
    private CommonCache<ConcurrentHashSet<Long>> userNumCache = CacheUtils.cacheBuilder(new CacheLoader<ConcurrentHashSet<Long>>() {
        @SuppressWarnings("unchecked")
        @Override
        public ConcurrentHashSet<Long> load(Object... keys) {
            List list = JdbcUtil.listIdByAttr(Entity.class, "num", keys[0]);
            if (list != null && list.size() > 0) {
                return new ConcurrentHashSet<Long>(list);
            } else {
                return new ConcurrentHashSet<Long>();
            }
        }
    }).build();


    @Override
    public void onIndexCreate(String name, Object value, Long key) {
        if (name.equals(Entity.NUM_INDEX)) {
            userNumCache.get(value).add(key);
        }
    }

    @Override
    public void onIndexRemove(String name, Object value, Long key) {
        if (name.equals(Entity.NUM_INDEX)) {
            userNumCache.get(value).remove(key);
        }
    }

    @Override
    public void onIndexChange(String name, Object oldValue, Object newValue, Long key) {
        if (name.equals(Entity.NUM_INDEX)) {
            this.onIndexCreate(name, newValue, key);
            this.onIndexRemove(name, oldValue, key);
        }
    }


    @org.junit.Test
    public void testChangeIndex() {

        ConcurrentHashSet<Long> entityIds = this.userNumCache.get(202);

        System.out.println(entityIds);

        Entity entity = this.dbCacheService.get(1l);
        entity.setNum(202);
        entity.setA(new byte[100]);


        entityIds = this.userNumCache.get(202);
        System.out.println(entityIds);

        this.dbCacheService.submitUpdate(entity);
        if (entity instanceof EnhancedEntity) {
            System.out.println(((EnhancedEntity)entity).getEntity());
        }

    }


}
