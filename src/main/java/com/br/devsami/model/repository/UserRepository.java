package com.br.devsami.model.repository;

import com.br.devsami.model.entity.User;
import com.br.devsami.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    // tudo responsavel consultar a existencia de usuários

    @SuppressWarnings("null")
    public Optional<User> findByCpf(String cpf) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.cpf = :cpf", User.class)
                    .setParameter("cpf", cpf)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    // procura se existe pelo cpf, o normal é ter só 1, mas qualquer coisa maior que
    // 0 é 1. é um retorno de verdadeiro ou falso
    public boolean existsByCpf(String cpf) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            @SuppressWarnings("null")
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf",
                    Long.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();

            return count > 0;
        } finally {
            em.close(); // isso é para fechar a conexão com o db
        }
    }

    // usa o uuid para pegar algum user
    @SuppressWarnings("null")
    public Optional<User> findById(UUID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, id));
        } finally {
            em.close();
        }
    }

    // pega todos os users que existir

    @SuppressWarnings("null")
    public List<User> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        executeInsideTransaction(em -> em.persist(user));
    }

    public User update(User user) {
        return executeInsideTransactionReturning(em -> em.merge(user));
    }

    public void delete(UUID id) {
        executeInsideTransaction(em -> {
            @SuppressWarnings("null")
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
            }
        });
    }

    // =========================
    // 🔧 Helpers (limpeza importante)
    // =========================

    private void executeInsideTransaction(java.util.function.Consumer<EntityManager> action) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private <T> T executeInsideTransactionReturning(java.util.function.Function<EntityManager, T> action) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            T result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}