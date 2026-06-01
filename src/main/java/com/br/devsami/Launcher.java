package com.br.devsami;

// import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            com.br.devsami.utils.HibernateUtil.close();
        })); // fechar o h2
        // Application.launch(App.class, args);

        App.main(args);
    }

}
