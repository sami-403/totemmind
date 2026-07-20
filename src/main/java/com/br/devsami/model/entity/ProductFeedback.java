package com.br.devsami.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PRODUCT")
@Getter
@Setter
@NoArgsConstructor
public class ProductFeedback extends Feedback {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
