package org.domain.dao;

import java.util.List;

import javax.ejb.Remove;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Transactional;

public interface SeamDAOLocal {

	public abstract Query createQuery(String query);

	public abstract SQLQuery createSQLQuery(String query);

	public abstract Query createNamedQuery(String name);

	public abstract <T> List<T> findByExample(T exemplo);

	public abstract <T> List<T> findAll(Class<T> entityClass);

	public abstract <T> T find(Class<T> entityClass, Object id);

	public abstract <T> T getReference(Class<T> entityClass, Object id);

	public abstract <T> T merge(T entity);

	@Transactional
	public abstract void persist(Object entity);

	public abstract void refresh(Object entity);

	public abstract void remove(Object entity);

	public abstract boolean contains(Object entity);

	public abstract void lock(Object entity, LockModeType lockMode);

	public abstract void flush();

	public abstract void clear();

	public abstract void close();

	public abstract Session getHibernateSession();

	public abstract Criteria createCriteria(Class<?> c);

	@Remove
	@Destroy
	public abstract void destroy();

	public abstract void setEntityManager(EntityManager entityManager);

}