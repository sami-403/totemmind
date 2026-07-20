package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.EmployeeFeedback;
import com.br.devsami.infrastructure.persistence.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    // O Feedback em si usa UUID
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

    // Busca os feedbacks pelo ID do funcionário (que é Long)
    public List<EmployeeFeedback> findByEmployeeId(Long employeeId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM EmployeeFeedback f WHERE f.employee.id = :employeeId", EmployeeFeedback.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Busca os feedbacks por funcionário (Long) dentro de um período
    public List<EmployeeFeedback> findByEmployeeAndPeriod(Long employeeId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    """
                            SELECT f FROM EmployeeFeedback f
                            WHERE f.employee.id = :employeeId
                            AND f.createdAt BETWEEN :start AND :end
                            """, EmployeeFeedback.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
