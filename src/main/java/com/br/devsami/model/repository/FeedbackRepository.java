package com.br.devsami.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.utils.HibernateUtil;

import jakarta.persistence.EntityManager;

public class FeedbackRepository {

    public void save(Feedback feedback) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(feedback);
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

    public Optional<Feedback> findById(UUID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Feedback.class, id));
        } finally {
            em.close();
        }
    }

    public List<Feedback> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT f FROM Feedback f", Feedback.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Pegar todos os feedbacks de um funcionário
    public List<Feedback> findByEmployeeId(UUID employeeId) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT f FROM Feedback f WHERE f.employee.id = :employeeId",
                    Feedback.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Buscar todos os feedbacks por periodo de tempo.
    public List<Feedback> findByPeriod(
            LocalDateTime start,
            LocalDateTime end) {

        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                    """
                SELECT f
                FROM Feedback f
                WHERE f.createdAt BETWEEN :start AND :end
                """,
                    Feedback.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Feedbacks de um funcionário especifico de acordo com o tempo:
    public List<Feedback> findByEmployeeAndPeriod(
            UUID employeeId,
            LocalDateTime start,
            LocalDateTime end) {

        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                    """
                SELECT f
                FROM Feedback f
                WHERE f.employee.id = :employeeId
                AND f.createdAt BETWEEN :start AND :end
                """,
                    Feedback.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
