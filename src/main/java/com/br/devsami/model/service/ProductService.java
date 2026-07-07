package com.br.devsami.model.service;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(){
        this.productRepository = new ProductRepository();
    }

    //Buscar Produto
    public Optional<Product> findByBarCode(String barCode){
        return productRepository.findByBarCode(barCode);
    }

    //Criar Produto
    public Product createProduct(String name, String barCode, double price){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        if (barCode == null || barCode.isBlank()) {
            throw new IllegalArgumentException("Código de Barras obrigatório");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Preço do Produto deve ser positivo");
        }

        if(productRepository.existsByBarCode(barCode)){
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        var product = new Product();
        product.setName(name);
        product.setBarCode(barCode);
        product.setPrice(BigDecimal.valueOf(price));

        productRepository.save(product);

        return product;
    }
}
