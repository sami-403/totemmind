package com.br.devsami.model.enums;

public enum EmployeeFeedbackCategory {
    COURTESY("Educação / Cordialidade / Respeito"),
    SPEED("Agilidade / Tempo de Espera"),
    COMMUNICATION("Clareza / Comunicação"),
    RESOLUTION("Resolução de Problemas"),
    PRAISE("Elogios / Positivo"),
    OTHER("Outros");

    private final String description;

    EmployeeFeedbackCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
