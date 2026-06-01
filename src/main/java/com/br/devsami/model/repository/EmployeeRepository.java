package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;

// Controla a exibição de funcionários.

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
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}