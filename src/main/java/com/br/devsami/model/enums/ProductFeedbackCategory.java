package com.br.devsami.model.enums;

public enum ProductFeedbackCategory {
    QUALITY("Qualidade / Sabor"),
    TEMPERATURE("Temperatura"),
    PORTION("Tamanho / Porção"),
    PACKAGING("Embalagem / Apresentação"),
    PRICE("Preço / Custo-benefício"),
    PRAISE("Elogios / Positivo"),
    OTHER("Outros");

    private final String description;

    ProductFeedbackCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
