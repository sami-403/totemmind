package com.br.devsami.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class Person {

    private String name;

    // define regras para cpf, deve ser unico, deve ter 11 caracteres, nao pode ser
    // nulo
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return String return the cpf
     */
    public String getCpf() {
        return cpf;
    }

    /**
     * @param cpf the cpf to set
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    // noramliza o cpf, tira pontos e traços
    @PrePersist
    @PreUpdate
    protected void normalizeCpf() {
        if (this.cpf != null) {
            this.cpf = this.cpf.replaceAll("[^0-9]", "");
        }
    }

}