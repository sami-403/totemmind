package com.br.devsami.model.entity;

import com.br.devsami.utils.enums.EmployeeType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity

    @Data
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
}