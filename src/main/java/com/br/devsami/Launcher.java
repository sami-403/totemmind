package com.br.devsami;

import com.br.devsami.utils.HibernateUtil;

public class Launcher {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::close));

        App.main(args);
    }

}
