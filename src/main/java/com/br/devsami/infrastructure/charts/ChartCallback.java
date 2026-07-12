package com.br.devsami.infrastructure.charts;

import java.util.Map;

public interface ChartCallback {
    void exibirGraficoPizza(String titulo, double[] percentagens);
    void exibirGraficoLinhas(String titulo, Map<String, int[]> dados);

}