package com.br.devsami.model.entity;

import com.br.devsami.model.enums.ProductFeedbackCategory;
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

    @Column(name = "rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    private ProductFeedbackCategory productCategory;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public ProductFeedbackCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductFeedbackCategory productCategory) {
        this.productCategory = productCategory;
    }
}
