package com.xipilli.persistence.dao;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NonUniqueResultException;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.xipilli.common.util.collection.SparseMatrixMap;
import com.xipilli.persistence.dao.util.DAOUtil;
import com.xipilli.persistence.dao.util.hql.HQLBuilder;
import com.xipilli.persistence.dao.util.hql.HQLTemplate;
import com.xipilli.persistence.model.AbstractPersistentEntity;

/**
 * Custom base dao for Spring+Hibernate support and proprietary entity
 * persistence handling.
 */
public abstract class AbstractBaseDAO<T extends AbstractPersistentEntity> implements IBaseDAO<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractBaseDAO.class);

    protected SessionFactory sessionFactory;

    // id property constant
    protected static final String ID = "id";
    protected static final String STATUS = "status";
    protected static final String CREATE_TIMESTAMP = "create_timestamp";
    protected static final String UPDATE_TIMESTAMP = "update_timestamp";

    @Autowired
    @Qualifier("sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    private static final SparseMatrixMap<Class, HQLBuilder, HQLTemplate> HQL_TEMPLATE_BUILDER_CACHE = new SparseMatrixMap<Class, HQLBuilder, HQLTemplate>();

    // hql template builders
    protected static final HQLBuilder FIND_BY_ID_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .staticPlaceholder(" = :property ");
    // all
    protected static final HQLBuilder FIND_ALL_HQL = HQLBuilder.fromEntity(
            " as model where model.").statusIsNotInactive();
    protected static final HQLBuilder FIND_ALL_SORTED_DESC_HQL = HQLBuilder
            .fromEntity(" as model where model.").statusIsNotInactive()
            .orderBy("model.").placeholder(" desc");
    protected static final HQLBuilder FIND_ALL_SORTED_ASC_HQL = HQLBuilder
            .fromEntity(" as model where model.").statusIsNotInactive()
            .orderBy("model.").placeholder(" asc");
    // property
    protected static final HQLBuilder FIND_BY_PROPERTY_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ");
    protected static final HQLBuilder FIND_BY_PROPERTY_LIKE_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .statusIsNotInactive(" and upper(model.").placeholder(") like :property ");
    protected static final HQLBuilder FIND_BY_PROPERTY_SORTED_DESC_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ")
            .orderBy("model.").placeholder(" desc");
    protected static final HQLBuilder FIND_BY_PROPERTY_SORTED_ASC_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ")
            .orderBy("model.").placeholder(" asc");
    // property mapped to support HibernateCallback scoped transactions
    protected static final HQLBuilder FIND_BY_PROPERTY_MAPPED_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ");
    protected static final HQLBuilder FIND_BY_PROPERTY_MAPPED_SORTED_DESC_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ")
            .orderBy("model.").placeholder(" desc");
    protected static final HQLBuilder FIND_BY_PROPERTY_SMAPPED_ORTED_ASC_HQL = HQLBuilder
            .fromEntity(" as model where model.")
            .placeholder(" = :property ")
            .orderBy("model.").placeholder(" asc");

    private final Class<T> supportedType;
    private final String supportedTypeIdProperty;

    public AbstractBaseDAO(Class<T> supportedType,
            String supportedTypeIdProperty) {
        this.supportedType = supportedType;
        this.supportedTypeIdProperty = supportedTypeIdProperty;
    }

    /**
     * Gets from or applies a template to the cache. Lets us avoid repeated
     * calls to Class.getSimpleName(), which does a bunch of String analysis.
     * Also static arguments are applied to the template. "Static" args are
     * lazily applied once to any template. Thereafter, only runtime args
     * involve any heavy string-analysis. So, there are a few levels of cost
     * savings built in here.
     *
     * @param hqlBuilder
     *            the builder of the template which to fetch
     * @param staticArgs
     *            is the Object[] of args to apply to the template at its static
     *            rendering
     * @return a built and cached {@see HQLTemplate}
     */
    private HQLTemplate getCachedTemplate(HQLBuilder hqlBuilder, Object... staticArgs) {
        HQLTemplate hqlTemplate = HQL_TEMPLATE_BUILDER_CACHE.get(supportedType, hqlBuilder);
        if (hqlTemplate == null) {
            hqlTemplate = hqlBuilder.buildHQLTemplateFor(supportedType, staticArgs);
            HQL_TEMPLATE_BUILDER_CACHE.put(supportedType, hqlBuilder, hqlTemplate);
        }
        return hqlTemplate;
    }

    /****** CRUD methods: *************************************************************************/

    public void saveNew(T transientInstance) {
        if (transientInstance.getStatus() == null) {
            transientInstance.setStatus(IBaseDAO.STATUS_ACTIVE);
        }
        Date now = DAOUtil.now();
        if (transientInstance.getCreateTimestamp() == null) {
            transientInstance.setCreateTimestamp(now);
        }
        if (transientInstance.getUpdateTimestamp() == null) {
            transientInstance.setUpdateTimestamp(now);
        }
        sessionFactory.getCurrentSession().save(transientInstance);
    }

    public final T findUniqueById(Object id) {
        log.debug(new StringBuilder("Getting instance with id: ").append(id).toString());
        String hqlString = getCachedTemplate(FIND_BY_ID_HQL, supportedTypeIdProperty).render();
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", id);
        return (T) resolveUnique(_objectToGenerics(query.list()));
    }

    protected final List<T> _objectToGenerics(List<Object> list){
        List<T> newList = new ArrayList<T>();
        for (Object o: list){
            newList.add((T) o);
        }
        return newList;
    }

    public final List<T> findAll() {
        log.debug("Finding all instances...");
        String hqlString = getCachedTemplate(FIND_ALL_HQL).render();
        return _objectToGenerics(sessionFactory.getCurrentSession().createQuery(hqlString).list());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ecomzero.persistence.dao.base.BaseDAO#findAllPaged(int, int)
     */
    @Override
    public final List<T> findAll(final int start, final int limit) {
        log.debug("Finding all instances and return pages...");
        String hqlString = getCachedTemplate(FIND_ALL_HQL).render();
        return _objectToGenerics(sessionFactory.getCurrentSession().createQuery(hqlString).setFirstResult(start).setMaxResults(limit).list());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ecomzero.persistence.dao.base.BaseDAO#findAllPagedSorted(int,
     * int, java.lang.String, java.lang.String)
     */
    @Override
    public final List<T> findAll(final int start, final int limit, final String sort, final String dir) {
        log.debug("Finding all instances and return sorted and pages...");
        String hqlString = getCachedTemplate(dir.toLowerCase().equals("desc") ? FIND_ALL_SORTED_DESC_HQL : FIND_ALL_SORTED_ASC_HQL).render(sort);
        return _objectToGenerics(sessionFactory.getCurrentSession().createQuery(hqlString).setFirstResult(start).setMaxResults(limit).list());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ecomzero.persistence.dao.base.BaseDAO#findAllSorted(java.lang.String,
     * java.lang.String)
     */
    @Override
    public final List<T> findAll(String sort, String dir) {
        log.debug(new StringBuilder("Finding all instance sorted by: ").append(sort).append(", direction: ").append(dir).append("...").toString());
        String hqlString = getCachedTemplate(dir.toLowerCase().equals("desc") ? FIND_ALL_SORTED_DESC_HQL : FIND_ALL_SORTED_ASC_HQL).render(sort);
        return _objectToGenerics(sessionFactory.getCurrentSession().createQuery(hqlString).list());
    }

    public final Long countAll() {
        log.debug("Counting all instances...");
        String hqlString = getCachedTemplate(FIND_ALL_HQL).renderCnt();
        List<Long> result = sessionFactory.getCurrentSession().createQuery(hqlString).list();
        Long count = (long) 0;
        if (null != result && result.size() > 0) {
            count = result.get(0);
        }
        return count;
    }

    public final List<T> findByProperty(String propertyName, Object value) {
        log.debug(new StringBuilder("Finding instance with property: ").append(propertyName).append(", value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(FIND_BY_PROPERTY_HQL).render(propertyName);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", value);
        return _objectToGenerics(query.list());
    }

    public List<T> findByPropertyLike(String propertyName, Object value) {
        log.debug(new StringBuilder("Finding instance with property: ").append(propertyName).append(", like value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(FIND_BY_PROPERTY_LIKE_HQL).render(propertyName);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", "%".concat(value.toString().toUpperCase()).concat("%"));
        return _objectToGenerics(query.list());
    }

    public final Long countByProperty(String propertyName, final Object value) {
        log.debug(new StringBuilder("Counting with property: ").append(propertyName).append(", value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(FIND_BY_PROPERTY_HQL).renderCnt(propertyName);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", value);
        List<Long> results = query.list();
        Long count = (long) 0;
        if (null != results && results.size() > 0) {
            count = results.get(0);
        }
        return count;
    }

    public final List<T> findByProperty(String propertyName, Object value, String sort, String dir) {
        log.debug(new StringBuilder("Finding instance with property: ").append(propertyName).append(", value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(dir.toLowerCase().equals("desc") ? FIND_BY_PROPERTY_SORTED_DESC_HQL : FIND_BY_PROPERTY_SORTED_ASC_HQL).render(propertyName, sort);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", value);
        return _objectToGenerics(query.list());
    }

    public final List<T> findByProperty(String propertyName, final Object value, final int start, final int limit) {
        log.debug(new StringBuilder("Finding instance with property: ").append(propertyName).append(", value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(FIND_BY_PROPERTY_MAPPED_HQL).render(propertyName);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", value);
        return _objectToGenerics(query.setFirstResult(start).setMaxResults(limit).list());
    }

    public final List<T> findByProperty(String propertyName, final Object value, final int start, final int limit, String sort,	String dir) {
        if (start == limit) {
            return this.findByProperty(propertyName, value, sort, dir);
        }
        log.debug(new StringBuilder("Finding instance with property: ").append(propertyName).append(", value: ").append(value).append("...").toString());
        String hqlString = getCachedTemplate(dir.toLowerCase().equals("desc") ? FIND_BY_PROPERTY_MAPPED_SORTED_DESC_HQL : FIND_BY_PROPERTY_SMAPPED_ORTED_ASC_HQL).render(propertyName, sort);
        Query query = sessionFactory.getCurrentSession().createQuery(hqlString);
        query.setParameter("property", value);
        return _objectToGenerics(query.setFirstResult(start).setMaxResults(limit).list());
    }

    public final T findUniqueByProperty(String propertyName, Object value) {
        return resolveUnique(findByProperty(propertyName, value));
    }

    public List<T> findByExample(T instance) {
        return findByExample(instance, new String[]{});
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T instance, String[] excludeProperty) {
        log.debug("finding instance by example");
        Criteria crit = getSession().createCriteria(supportedType);
        Example example = Example.create(instance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    public T findUniqueByExample(T instance) {
        return (T) resolveUnique(findByExample(instance));
    }

    public final Long countByCriterion(final Criterion... criterion) {
        log.debug("Counting with criterion");
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(supportedType);
        crit.setProjection(Projections.rowCount());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return (Long)crit.uniqueResult();
    }

    public List<T> findByCriterion(final Criterion... criterion) {
        DetachedCriteria crit = DetachedCriteria.forClass(supportedType);
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return findByDetachedCriteria(crit);
    }

    public List<T> findByDetachedCriteria(final DetachedCriteria criteria) {
        Criteria crit = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
        return crit.list();
    }

    public T findUniqueByCriterion(Criterion... criterion) {
        return (T) resolveUnique(findByCriterion(criterion));
    }

    public final void attachDirty(T instance) {
        log.debug("Attaching dirty instance...");
        if (instance.getCreateTimestamp() == null || instance.getStatus() == null) {
            saveNew(instance);
        } else {
            instance.setUpdateTimestamp(DAOUtil.now());
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
        }
    }

    public final void attachClean(T instance) {
        log.debug("attaching clean instance");
        sessionFactory.getCurrentSession().buildLockRequest(new LockOptions(LockMode.NONE)).lock(instance);
    }

    @SuppressWarnings("unchecked")
    public final T merge(T detachedInstance) {
        log.debug("merging instance");
        if (detachedInstance.getStatus() == null) {
            detachedInstance.setStatus(IBaseDAO.STATUS_ACTIVE);
        }
        Date now = DAOUtil.now();
        if (detachedInstance.getCreateTimestamp() == null) {
            detachedInstance.setCreateTimestamp(now);
        }
        if (detachedInstance.getUpdateTimestamp() == null) {
            detachedInstance.setUpdateTimestamp(now);
        }
        return (T)sessionFactory.getCurrentSession().merge(detachedInstance);
    }

    public final void delete(T persistentInstance) {
        log.debug("Deleting instance...");
        sessionFactory.getCurrentSession().delete(persistentInstance);
    }

    public final void diactivate(T persistentInstance) {
        log.debug("Deactivate instance...");
        persistentInstance.setStatus(IBaseDAO.STATUS_INACTIVE);
        persistentInstance.setUpdateTimestamp(DAOUtil.now());
        sessionFactory.getCurrentSession().saveOrUpdate(persistentInstance);
    }

    public final void activate(T persistentInstance) {
        log.debug("Activate instance...");
        persistentInstance.setStatus(IBaseDAO.STATUS_ACTIVE);
        persistentInstance.setUpdateTimestamp(DAOUtil.now());
        sessionFactory.getCurrentSession().saveOrUpdate(persistentInstance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null");
        }
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }

    /****** Helper methods: ***********************************************************************/

    public List<T> findByStatus(Object status) {
        return findByProperty(STATUS, status);
    }

    public T findUniqueByStatus(Object status) {
        return findUniqueByProperty(STATUS, status);
    }

    /**
     * Resolves an expected unique result from a results list.
     *
     * @param <T>
     * @param list
     * @return the sole result of type <X> or null if no results existed
     * @throws NonUniqueResultException
     *             if more than a single result were available.
     */
    public final T resolveUnique(List<T> list) {
        return DAOUtil.resolveUnique(list);
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}