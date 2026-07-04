package com.br.devsami.model.enums;

public enum FeedbackCategory {
    DELAY("Demora"),
    POLITENESS("Educação"),
    ENVIRONMENT("Ambiente"),
    SERVICE_QUALITY("Qualidade do Atendimento"),
    PRICE("Preço"),
    OTHER("Outros");

    private final String description;

    FeedbackCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
