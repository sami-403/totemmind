package com.br.devsami.model.entity;

import com.br.devsami.model.enums.EmployeeType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
}