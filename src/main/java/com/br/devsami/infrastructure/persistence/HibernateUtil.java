package com.br.devsami.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.concurrent.CompletableFuture;

public class HibernateUtil {
    private static EntityManagerFactory emf;
    private static final Object LOCK = new Object();

    public static void initializeAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                getEntityManagerFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (LOCK) {
                if (emf == null) {
                    try {
                        emf = Persistence.createEntityManagerFactory("meuPU");
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Erro ao inicializar o EntityManagerFactory", e);
                    }
                }
            }
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        synchronized (LOCK) {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}