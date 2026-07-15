package com.br.devsami.model.service;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.repository.ProductRepository;
import javafx.collections.transformation.SortedList;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(){
        this.productRepository = new ProductRepository();
    }

    //Listar Produtos
    public List<Product> listProducts(int page, int pageSize){
        int validatedPage = page < 1 ? 0: page;

        return productRepository.findAll(pageSize, validatedPage);
    }

    public List<Product> listProducts(int page, int pageSize, String sortBy, boolean reverse){
        int validatedPage = page < 1 ? 0: page;

        return productRepository.findAllSorted(pageSize, validatedPage, sortBy, reverse);
    }


    public int countPages(int pageSize){
        Long entriesCount = productRepository.countEntries();

        return (int) Math.ceil((double) entriesCount / pageSize);
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

    //Atualiza um Produto
    public Product updateProduct(UUID id, String newName, String newBarCode, double newPrice){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto com esse ID não encontrado."));

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        if (newBarCode == null || newBarCode.isBlank()) {
            throw new IllegalArgumentException("Código de Barras obrigatório");
        }

        if (newPrice < 0) {
            throw new IllegalArgumentException("Preço do Produto deve ser positivo");
        }

        if(productRepository.existsByBarCode(newBarCode) && product.getBarCode().equals(newBarCode.strip())){
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        product.setName(newName);
        product.setBarCode(newBarCode);
        product.setPrice(BigDecimal.valueOf(newPrice));

        productRepository.update(product);

        return product;
    }

    //Remove um produto
    public void deleteProduct(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto com esse ID não encontrado."));

        productRepository.delete(product);
    }
}
