package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/*
 * Repository responsável exclusivamente pelo acesso ao banco de dados
 * relacionado à entidade Employee.
 *
 * Aqui NÃO entram regras de negócio, apenas operações de persistência e consulta.
 */
public class EmployeeRepository {

    /*
     * Busca um funcionário pelo CPF.
     *
     * Usado principalmente no login e validação de existência.
     */
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

    /*
     * Verifica se já existe um funcionário com o CPF informado.
     *
     * Evita duplicidade antes de salvar novos registros.
     */
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

    /*
     * Persiste um novo funcionário no banco de dados.
     */
    public void save(Employee employee) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(employee);
            em.getTransaction().commit();
        } catch (Exception e) {
            // garante rollback em caso de falha para evitar estado inconsistente
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /*
     * Busca um funcionário pelo ID.
     *
     * Retorna Optional para evitar null e forçar o tratamento explícito.
     */
    public Optional<Employee> findById(long id) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return Optional.ofNullable(em.find(Employee.class, id));
        } finally {
            em.close();
        }
    }

    // buscar por nome (Devolve uma lista dos funcionários com aquele nome)
    public List<Employee> findByName(String name) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(:name)",
                            Employee.class)
                    .setParameter("name", "%" + name.trim() + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}