package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.EmployeeFeedback;
import com.br.devsami.model.entity.ProductFeedback;
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

    public void update(Feedback feedback) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(feedback);
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

    public void delete(Feedback feedback) {
        if (feedback == null || feedback.getId() == null) return;
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Feedback managed = em.find(Feedback.class, feedback.getId());
            if (managed != null) {
                if (managed.getUser() != null && managed.getUser().getFeedbacks() != null) {
                    managed.getUser().getFeedbacks().remove(managed);
                }
                em.remove(managed);
            }
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

    public List<EmployeeFeedback> findAllEmployeeFeedbacks() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT f FROM EmployeeFeedback f", EmployeeFeedback.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<EmployeeFeedback> findAllEmployeeFeedbacksByPeriod(LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM EmployeeFeedback f WHERE f.createdAt BETWEEN :start AND :end", EmployeeFeedback.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Busca os feedbacks pelo ID do produto (UUID)
    public List<ProductFeedback> findByProductId(UUID productId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM ProductFeedback f WHERE f.product.id = :productId", ProductFeedback.class)
                    .setParameter("productId", productId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Calcula a média das notas de estrelas de um produto
    public Double findAverageRatingByProduct(UUID productId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Double avg = em.createQuery(
                    "SELECT AVG(f.rating) FROM ProductFeedback f WHERE f.product.id = :productId AND f.rating IS NOT NULL", Double.class)
                    .setParameter("productId", productId)
                    .getSingleResult();
            return avg != null ? avg : 0.0;
        } finally {
            em.close();
        }
    }

    public List<ProductFeedback> findAllProductFeedbacks(LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            if (start != null && end != null) {
                return em.createQuery(
                        "SELECT f FROM ProductFeedback f WHERE f.createdAt BETWEEN :start AND :end", ProductFeedback.class)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();
            } else {
                return em.createQuery("SELECT f FROM ProductFeedback f", ProductFeedback.class)
                        .getResultList();
            }
        } finally {
            em.close();
        }
    }

    public List<ProductFeedback> findProductFeedbacksByProductAndPeriod(UUID productId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            if (start != null && end != null) {
                return em.createQuery(
                        "SELECT f FROM ProductFeedback f WHERE f.product.id = :productId AND f.createdAt BETWEEN :start AND :end", ProductFeedback.class)
                        .setParameter("productId", productId)
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();
            } else {
                return findByProductId(productId);
            }
        } finally {
            em.close();
        }
    }
}
