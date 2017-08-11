package com.xipilli.persistence.dao.util.hql;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * An implementation that frameworks-in the caching of an hql statement for application in the do
 * method.
 */
public abstract class HQLCachingHibernateCallback<T> implements HibernateCallback<T> {

    private String hql;

    /**
     * The primary controller.
     */
    @Override
    public final T doInHibernate(Session session) throws HibernateException, SQLException {
        if (hql == null) {
            //cache it
            hql = makeHql();
        }
        //pass it
        return doInHibernate(session, hql);
    }

    /**
     * Hook to return hql to be cached.
     *
     * @return the hql String
     */
    protected abstract String makeHql();

    /**
     * Hook to allow custom implementation override, if need be.  Default returns a list of results
     * of the cached hql.
     *
     * @param session
     * @param the precached hql
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
    protected T doInHibernate(Session session, String hql) throws HibernateException, SQLException {
        //default behavior, feel free to override
        Query hquery = session.createQuery(hql);
        return (T) hquery.list();
    }

}