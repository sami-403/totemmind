package com.br.devsami.model.repository;

import java.util.List;
import java.util.Optional;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.infrastructure.persistence.HibernateUtil;
import jakarta.persistence.EntityManager;

public class EmployeeRepository {

    public Optional<Employee> findByCpf(String cpf) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE e.cpf = :cpf", Employee.class)
                    .setParameter("cpf", cpf)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    public boolean existsByCpf(String cpf) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(e) FROM Employee e WHERE e.cpf = :cpf", Long.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public void save(Employee employee) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(employee);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(Employee employee) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(employee);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Employee employee) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            Employee managedEmployee = em.contains(employee) ? employee : em.merge(employee);
            em.remove(managedEmployee);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findById(long id) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    public List<Employee> findByName(String name) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(:name) AND e.ativo = true", Employee.class)
                    .setParameter("name", "%" + name.trim() + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Employee> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e", Employee.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Employee> findAllActive() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e WHERE e.ativo = true", Employee.class).getResultList();
        } finally {
            em.close();
        }
    }
}