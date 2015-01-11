package dbcache.support.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jdbc Dao支持
 * Created by Jake on 2015/1/10.
 */
@Component
public class JdbcSupport {


    @Autowired
    private DataSource dataSource;

    /** 实体信息缓存 */
    private Map<Class<?>, ModelInfo> modelInfoCache = new ConcurrentHashMap<Class<?>, ModelInfo>();


    /**
     * 获取或创建实体信息
     * @param clzz 实体类
     * @return
     */
    public ModelInfo getOrCreateModelInfo(Class<?> clzz) {

        return null;
    }



}
