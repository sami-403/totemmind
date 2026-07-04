package com.br.devsami.model.enums;

public enum Feelling {
    SATISFIED("Satisfeito"),
    DISSATISFIED("Insatisfeito"),
    NEUTRAL("Neutro");

    private final String valor;

    Feelling(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
