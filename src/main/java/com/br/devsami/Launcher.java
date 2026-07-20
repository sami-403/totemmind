package com.br.devsami;

import com.br.devsami.infrastructure.persistence.HibernateUtil;

public class Launcher {
    public static void main(String[] args) {
        // Inicializa o Hibernate de forma assíncrona em background
        HibernateUtil.initializeAsync();

        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::close));

        App.main(args);
    }
}
