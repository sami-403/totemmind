package com.br.devsami.infrastructure.charts;

import javafx.application.Platform;

public class ChartManager {
    private static ChartCallback telaCallback;

    public static void registrarTela(ChartCallback callback) {
        telaCallback = callback;
    }

    public static void exibirPizza(String titulo, double[] dados) {
        if (telaCallback != null) {
            Platform.runLater(() -> telaCallback.exibirGraficoPizza(titulo, dados));
        }
    }
}
