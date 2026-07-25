package com.br.devsami.model.service;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    // Listar Produtos
    public List<Product> listProducts(int page, int pageSize) {
        int pagesCount = countPages(pageSize);
        int validatedPage = page < 1 ? 0 : page;

        if(page > pagesCount){
            validatedPage = pagesCount;
        }

        return productRepository.findAll(pageSize, validatedPage);
    }

    public List<Product> listProducts(int page, int pageSize, String sortBy, boolean reverse) {
       int pagesCount = countPages(pageSize);
       int validatedPage = page < 1 ? 0 : page;

       if(page > pagesCount){
           validatedPage = pagesCount;
       }

        return productRepository.findAllSorted(pageSize, validatedPage, sortBy, reverse);
    }

    public int countPages(int pageSize) {
        Long entriesCount = productRepository.countEntries();

        return (int) Math.ceil((double) entriesCount / pageSize);
    }

    // Buscar Produto
    public Product findByBarCode(String barCode) {
        Optional<Product> foundProduct = productRepository.findByBarCode(barCode);

        if (barCode.isEmpty()){
            throw new IllegalArgumentException("Código de barras vazio ou invalido");
        }

        if(foundProduct.isEmpty()){
            throw new NoSuchElementException("Produto com este código não encontrado ou inexistente");
        }

        return foundProduct.get();
    }

    public Product findById(UUID id) {
        Optional<Product> foundProduct = productRepository.findById(id);

        if(foundProduct.isEmpty()){
            throw new NoSuchElementException("Produto com este ID não encontrado ou inexistente");
        }

        return foundProduct.get();
    }

    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    // Criar Produto
    public Product createProduct(String name, String barCode, double price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Preço do Produto deve ser positivo");
        }

        if (productRepository.existsByBarCode(barCode)) {
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        var product = new Product();
        product.setName(name);
        product.setBarCode(!barCode.isEmpty() ? barCode: null);
        product.setPrice(BigDecimal.valueOf(price));

        productRepository.save(product);

        return product;
    }

    // Atualiza um Produto
    public Product updateProduct(UUID id, String newName, String newBarCode, double newPrice) {
        Product product = productRepository.findById(id).orElseThrow();

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        if (newPrice < 0) {
            throw new IllegalArgumentException("Preço do Produto deve ser positivo");
        }

        if (productRepository.existsByBarCode(newBarCode) && !product.getBarCode().equals(newBarCode.strip())) {
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        product.setName(newName);
        product.setBarCode(newBarCode);
        product.setPrice(BigDecimal.valueOf(newPrice));

        productRepository.update(product);

        return product;
    }

    // Remove um produto
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto com esse ID não encontrado."));

        productRepository.delete(product);
    }
}
