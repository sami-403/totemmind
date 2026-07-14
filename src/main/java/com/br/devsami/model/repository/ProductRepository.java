package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Product;
import com.br.devsami.infrastructure.persistence.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ProductRepository {

    public void save(Product product){
        EntityManager em = HibernateUtil.getEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
        }
        catch (Exception e){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }

    public void update(Product product){
        EntityManager em = HibernateUtil.getEntityManager();
        try{
            em.getTransaction().begin();
            em.merge(product);
            em.getTransaction().commit();
        }
        catch (Exception e){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }

    public void delete(Product product){
        EntityManager em = HibernateUtil.getEntityManager();
        try{
            em.getTransaction().begin();
            Product managedProduct = em.contains(product) ? product : em.merge(product);
            em.remove(managedProduct);
            em.getTransaction().commit();
        }
        catch (Exception e){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }

    public Optional<Product> findById(UUID id){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return Optional.ofNullable(em.find(Product.class, id));
        }
    }

    public Optional<Product> findByBarCode(@NonNull String barCode){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.barCode LIKE :barCode",
                    Product.class)
                    .setParameter("barCode", barCode.trim())
                    .getResultStream()
                    .findFirst();
        }
    }

    public boolean existsByBarCode(@NonNull String barCode){
        try(EntityManager em = HibernateUtil.getEntityManager();){
           Long count = em.createQuery(
                            "SELECT COUNT(p) FROM Product p WHERE p.barCode LIKE :barCode",
                            Long.class)
                    .setParameter("barCode", barCode.trim())
                    .getSingleResult();
           return count > 0;
        }
    }

    public List<Product> findByName(@NonNull String name){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(
                            "SELECT p FROM Product p WHERE p.name LIKE :name",
                            Product.class)
                    .setParameter("name", name.trim())
                    .getResultList();
        }
    }

    public List<Product> findAll(){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(
                            "SELECT p FROM Product p",
                            Product.class)
                    .getResultList();
        }
    }

    public List<Product> findAll(int pageSize, int page){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(
                            "SELECT p FROM Product p",
                            Product.class)
                    .setFirstResult(page*pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
    }

    public Long countEntries(){
        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(
                   "SELECT COUNT(p) FROM Product p",
                   Long.class)
                    .getSingleResult();
        }
    }

}
