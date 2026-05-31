package com.br.devsami.utils.enums;

public enum EmployeeType {
    VENDEDOR("vendedor"),
    GERENTE("gerente");

    private final String tipo;

    EmployeeType(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return tipo;
    }
}
