package dbcache.support.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Jdbc Dao支持
 * Created by Jake on 2015/1/10.
 */
@Component
public class DaoSupport {


    @Autowired
    private DataSource dataSource;



}
