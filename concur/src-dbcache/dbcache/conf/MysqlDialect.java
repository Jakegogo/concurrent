package dbcache.conf;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * 
 * 增加MYSQL对 HIBERNATE本地SQL查询结果集中返回 text,longtext,blob,longblob支持
 *
 */
public class MysqlDialect extends MySQL5InnoDBDialect {
	
	public MysqlDialect(){
		super();
		super.registerHibernateType(Types.LONGNVARCHAR, Hibernate.STRING.getName());
		super.registerHibernateType(Types.LONGVARCHAR, Hibernate.STRING.getName());
		super.registerHibernateType(Types.LONGVARBINARY, Hibernate.BINARY.getName());
	}

}
