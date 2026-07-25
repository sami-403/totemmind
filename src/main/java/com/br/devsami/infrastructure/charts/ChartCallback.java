package com.br.devsami.infrastructure.charts;

import com.br.devsami.model.dto.ProductCardData;
import java.util.List;
import java.util.Map;

public interface ChartCallback {
    void exibirGraficoPizza(String titulo, double[] percentagens);
    void exibirGraficoLinhas(String titulo, Map<String, int[]> dados);
    void exibirCardsProdutos(String titulo, List<ProductCardData> produtos);
}