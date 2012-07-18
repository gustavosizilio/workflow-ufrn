package org.domain.dao;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remove;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.ejb.EntityManagerImpl;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

@Name("seamDao")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SeamDAO implements Serializable, SeamDAOLocal{

	private static final long serialVersionUID = -5025840242524481093L;
	
	@In("entityManager")
	private EntityManager entityManager;
	
	public Query createQuery(String query) {
		return entityManager.createQuery(query);
	}

	public SQLQuery createSQLQuery(String query) {
		return getHibernateSession().createSQLQuery(query);
	}
	
	public Query createNamedQuery(String name) {
		return entityManager.createNamedQuery(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> findByExample(T exemplo) {
    	Session hibernateSession = getHibernateSession();
    	Example hibernateExample = Example.create(exemplo);
    	Criteria hibernateCriteria = hibernateSession.createCriteria(exemplo.getClass()).add(hibernateExample);
    	return hibernateCriteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(Class<T> entityClass) {
		return (List<T>) entityManager.createQuery("From " + entityClass.getName()).getResultList();
	}
	
	public <T> T find(Class<T> entityClass, Object id) {
		return entityManager.find(entityClass, id);
	}

	public <T> T getReference(Class<T> entityClass, Object id) {
		return entityManager.getReference(entityClass, id);
	}
	
	public <T> T merge(T entity) {
		return entityManager.merge(entity);
	}

	@Transactional
	public void persist(Object entity) {
		entityManager.persist(entity);
	}

	public void refresh(Object entity) {
		entityManager.refresh(entity);
	}

	public void remove(Object entity) {
		entityManager.remove(entity);
	}

	public boolean contains(Object entity) {
		return entityManager.contains(entity);
	}
	
	public void lock(Object entity, LockModeType lockMode) {
		entityManager.lock(entity, lockMode);
	}

	public void flush() {
		entityManager.flush();
	}
	
	public void clear() {
		entityManager.clear();
	}

	public void close() {
		entityManager.close();
	}
	
	public Session getHibernateSession() {
		Object delegate = entityManager.getDelegate();
		if( delegate == null ) {
			throw new UnsupportedOperationException();
		} else if ( delegate instanceof EntityManagerImpl ) { 
			return ((EntityManagerImpl) delegate).getSession();
		} else if ( delegate instanceof Session ) {
    		return	(Session) delegate;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Criteria createCriteria(Class<?> c) {
		Session hibernateSession = getHibernateSession();
		return hibernateSession.createCriteria(c).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	}
	
	@Remove @Destroy
	public void destroy(){
	}
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}


