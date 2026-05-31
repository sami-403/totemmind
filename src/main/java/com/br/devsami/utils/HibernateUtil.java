package com.br.devsami.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("meuPU");

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void close() {
        EMF.close();
    }
}