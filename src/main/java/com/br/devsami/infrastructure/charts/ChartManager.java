package com.br.devsami.infrastructure.charts;

import javafx.application.Platform;

import java.util.Map;

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

    public static void exibirLinhas(String titulo, Map<String, int[]> dados) {
        if (telaCallback != null) {
            Platform.runLater(() -> telaCallback.exibirGraficoLinhas(titulo, dados));
        }
    }
}
