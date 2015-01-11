package dbcache.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

/**
 * HibernateUtils
 * Created by Jake on 2014/12/28.
 */
@Component
public class HibernateUtil extends DaoSupport {

    // HibernateTemplate
    private static HibernateTemplate hibernateTemplate;


    /**
     * Return the Hibernate SessionFactory used by this DAO.
     */
    public static final SessionFactory getSessionFactory() {
        return (hibernateTemplate != null ? hibernateTemplate.getSessionFactory() : null);
    }

    /**
     * Set the HibernateTemplate for this DAO explicitly,
     * as an alternative to specifying a SessionFactory.
     */
    public static final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        HibernateUtil.hibernateTemplate = hibernateTemplate;
    }

    public static final HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }


    public static final Session getSession()
            throws DataAccessResourceFailureException, IllegalStateException {

        return getSession(hibernateTemplate.isAllowCreate());
    }

    public static final Session getSession(boolean allowCreate)
            throws DataAccessResourceFailureException, IllegalStateException {

        return (!allowCreate ?
                SessionFactoryUtils.getSession(getSessionFactory(), false) :
                SessionFactoryUtils.getSession(
                        getSessionFactory(),
                        hibernateTemplate.getEntityInterceptor(),
                        hibernateTemplate.getJdbcExceptionTranslator()));
    }


    @Override
    protected final void checkDaoConfig() {
        if (this.hibernateTemplate == null) {
            throw new IllegalArgumentException("'sessionFactory' or 'hibernateTemplate' is required");
        }
    }

    @Autowired
    public void setSessionFactory0(SessionFactory sessionFactory){
        if (hibernateTemplate == null || sessionFactory != hibernateTemplate.getSessionFactory()) {
            hibernateTemplate = createHibernateTemplate(sessionFactory);
        }
    }

    protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }


}
