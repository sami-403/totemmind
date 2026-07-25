package com.br.devsami.model.repository;

import com.br.devsami.model.entity.Product;
import com.br.devsami.infrastructure.persistence.HibernateUtil;
import com.br.devsami.util.BarCodeValidator;
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

    public boolean existsByBarCode(String barCode){
        if(BarCodeValidator.isEmptyOrNull(barCode)) return false;

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

    public List<Product> findByNameContainingIgnoreCase(@NonNull String name) {
        String cleanName = name.trim().toLowerCase()
                .replace("á", "a").replace("à", "a").replace("ã", "a").replace("â", "a")
                .replace("é", "e").replace("ê", "e").replace("í", "i")
                .replace("ó", "o").replace("ô", "o").replace("õ", "o")
                .replace("ú", "u").replace("ç", "c");

        List<String> terms = new java.util.ArrayList<>();
        terms.add(cleanName);
        if (cleanName.contains("hamburguer") || cleanName.contains("burger") || cleanName.contains("burguer")) {
            terms.add("burguer");
            terms.add("burger");
            terms.add("hamburguer");
        }

        try (EntityManager em = HibernateUtil.getEntityManager()) {
            List<Product> all = em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
            List<Product> matches = new java.util.ArrayList<>();
            for (Product p : all) {
                String pNameClean = p.getName().toLowerCase()
                        .replace("á", "a").replace("à", "a").replace("ã", "a").replace("â", "a")
                        .replace("é", "e").replace("ê", "e").replace("í", "i")
                        .replace("ó", "o").replace("ô", "o").replace("õ", "o")
                        .replace("ú", "u").replace("ç", "c");

                for (String term : terms) {
                    if (pNameClean.contains(term)) {
                        matches.add(p);
                        break;
                    }
                }
            }
            return matches;
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

    public List<Product> findAllSorted(int pageSize, int page, String sortBy, boolean desc){
        String orderDirection = desc ? "DESC" : "ASC";
        String sortField = sortBy.equalsIgnoreCase("price") ? "price" : "name";

        String query = "SELECT p FROM Product p ORDER BY p." + sortField + " " + orderDirection;

        try(EntityManager em = HibernateUtil.getEntityManager();){
            return em.createQuery(query, Product.class)
                    .setFirstResult(page * pageSize)
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
