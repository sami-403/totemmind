package com.br.devsami.model.service;

import javafx.collections.FXCollections;
import javafx.scene.chart.PieChart;

public class ChartGeneratorService {

    public PieChart criarGraficoSatisfacao(double satisfeito, double neutro, double insatisfeito) {
        PieChart chart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Satisfeito", satisfeito),
                new PieChart.Data("Neutro", neutro),
                new PieChart.Data("Insatisfeito", insatisfeito)));

        chart.setTitle("Satisfação Geral");
        chart.setLabelsVisible(true);
        chart.setPrefSize(300, 300);

        return chart;
    }
}