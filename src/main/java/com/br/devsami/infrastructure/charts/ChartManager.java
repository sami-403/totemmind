package com.br.devsami.infrastructure.charts;

import com.br.devsami.model.dto.EmployeeCardData;
import com.br.devsami.model.dto.ProductCardData;
import javafx.application.Platform;

import java.util.List;
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

    public static void exibirCardsProdutos(String titulo, List<ProductCardData> produtos) {
        if (telaCallback != null) {
            Platform.runLater(() -> telaCallback.exibirCardsProdutos(titulo, produtos));
        }
    }

    public static void exibirCardsFuncionarios(String titulo, List<EmployeeCardData> funcionarios) {
        if (telaCallback != null) {
            Platform.runLater(() -> telaCallback.exibirCardsFuncionarios(titulo, funcionarios));
        }
    }
}
