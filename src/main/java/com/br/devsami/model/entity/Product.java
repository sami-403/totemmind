package com.br.devsami.model.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
    @NoArgsConstructor

    public class Product {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 13, unique = true)
    private String codigoBarras;

    @Column(nullable = false)
    private String nome;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal preco;

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Product(String codigoBarras, String nome, BigDecimal preco) {
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.preco = preco;
    }

}
