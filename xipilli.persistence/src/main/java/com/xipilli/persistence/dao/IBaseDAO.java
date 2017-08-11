package com.xipilli.persistence.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;

import com.xipilli.persistence.model.AbstractPersistentEntity;

/**
 * Custom base dao for Spring+Hibernate support and proprietary entity persistence handling. Most
 * requests should utilize these calls directly. Any absolutely necessary hql string querying needs
 * to be careful to manage for deleted statuses.
 *
 */
public interface IBaseDAO<T extends AbstractPersistentEntity> {
	public static final String	STATUS_ACTIVE	= "A";
	public static final String	STATUS_INACTIVE	= "I";
	public static final String	STATUS_LOCKED	= "L";

	public static final Boolean	ENABLED_YES		= true;
	public static final Boolean	ENABLED_NO		= false;

	public static final Boolean	TRUE			= true;
	public static final Boolean	FALSE			= false;

	public static final String	ASCENDING		= "ASC";
	public static final String	DESCENDING		= "DESC";

    void saveNew(T transientInstance);

    T findUniqueById(Object id);

    List<T> findAll();

    List<T> findByProperty(String propertyName, Object value);

    List<T> findByProperty(String propertyName, Object value, String sort, String dir);

    List<T> findByProperty(String propertyName, Object value, int start, int limit);

    List<T> findByProperty(String propertyName, Object value, int start, int limit, String sort, String dir);

    T findUniqueByProperty(String propertyName, Object value);

    List<T> findByPropertyLike(String propertyName, Object value);

    void attachDirty(T instance);

    T merge(T detachedInstance);

    void attachClean(T instance);

    void delete(T persistentInstance);

    void diactivate(T persistentInstance);

    void activate(T persistentInstance);

    /**
     * @param start
     * @param limit
     * @return
     */
    List<T> findAll(int start, int limit);

    /**
     * @param start
     * @param limit
     * @param sort
     * @param dir
     * @return
     */
    List<T> findAll(int start, int limit, String sort, String dir);

    /**
     * @param sort
     * @param dir
     * @return
     */
    List<T> findAll(String sort, String dir);

    /**
     * @param object
     * @return
     */
    List<T> findByExample(T instance);
    T findUniqueByExample(T instance);

    /**
     * @param criterion
     * @return
     */
    List<T> findByCriterion(Criterion... criterion);

    List<T> findByDetachedCriteria(DetachedCriteria criteria);

    /**
     * @param criterion
     * @return
     */
    T findUniqueByCriterion(Criterion... criterion);

    Long countByCriterion(final Criterion... criterion);

    Long countAll();

    Long countByProperty(String propertyName, final Object value);
}