package com.br.devsami.model.entity;

import com.br.devsami.model.enums.EmployeeType;

import jakarta.persistence.*;
import lombok.*;

@Entity

    @NoArgsConstructor
    @AllArgsConstructor
    public class Employee extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EmployeeType tipo = EmployeeType.VENDEDOR;

    public Long getId() {
        return id;
    }

    public EmployeeType getTipo() {
        return tipo;
    }

    public void setTipo(EmployeeType tipo) {
        this.tipo = tipo;
    }

    public Object getType() {
        return tipo;
    }

    // O ideal é que por padrão os funcionarios comecem ativos
    @Column(name = "ativo")
    private boolean ativo = true;

    // ... gere os getters e setters para este campo
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}