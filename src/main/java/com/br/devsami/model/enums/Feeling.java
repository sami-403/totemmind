package com.br.devsami.model.enums;

public enum Feeling {
    SATISFIED("Satisfeito"),
    DISSATISFIED("Insatisfeito"),
    NEUTRAL("Neutro");

    private final String valor;

    Feeling(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
