package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

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
}