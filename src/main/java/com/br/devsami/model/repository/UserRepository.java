package com.br.devsami.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

import com.br.devsami.model.entity.User;
import com.br.devsami.infrastructure.persistence.HibernateUtil;

import jakarta.persistence.EntityManager;

public class UserRepository {

    // tudo responsavel consultar a existencia de usuários

    // Busca um usuário pelo CPF.
    // O retorno é Optional porque o usuário pode não existir.
    @SuppressWarnings("null")
    public Optional<User> findByCpf(String cpf) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.cpf = :cpf", User.class)
                    .setParameter("cpf", cpf)
                    .getResultStream()
                    .findFirst();
        }
    }

    // procura se existe pelo cpf, o normal é ter só 1, mas qualquer coisa maior que
    // 0 é 1. é um retorno de verdadeiro ou falso
    // Verifica se existe um usuário com o CPF informado.
    public boolean existsByCpf(String cpf) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            @SuppressWarnings("null")
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf",
                    Long.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();

            return count > 0;
        }
    }

    // usa o uuid para pegar algum user
    // Busca um usuário pelo UUID.
    @SuppressWarnings("null")
    public Optional<User> findById(UUID id) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(User.class, id));
        }
    }

    // pega todos os users que existir

    // Busca todos os usuários existentes.
    @SuppressWarnings("null")
    public List<User> findAll() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
        }
    }

    public void save(@NonNull User user) {
        executeInsideTransaction(em -> em.persist(user));
    }

    public User update(@NonNull User user) {
        return executeInsideTransactionReturning(em -> em.merge(user));
    }

    public void delete(@NonNull UUID id) {
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