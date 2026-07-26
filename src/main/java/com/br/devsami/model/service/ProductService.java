package com.br.devsami.model.service;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.repository.ProductRepository;
import com.br.devsami.util.BarCodeValidator;
import com.br.devsami.util.PriceValidator;
import java.math.BigDecimal;
import java.util.*;

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
        int pagesCount = (int) Math.ceil((double) entriesCount / pageSize);

        return pagesCount > 0 ? pagesCount: 1;
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
    public Product createProduct(String name, String barCode, String price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        PriceValidator.validate(price);
        BarCodeValidator.validate(barCode);

        if (productRepository.existsByBarCode(barCode)) {
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        var product = new Product();
        product.setName(name);
        product.setBarCode(!barCode.isEmpty() ? barCode: null);
        product.setPrice(PriceValidator.parsePrice(price));

        productRepository.save(product);

        return product;
    }

    // Atualiza um Produto
    public Product updateProduct(UUID id, String newName, String newBarCode, String newPrice) {
        Product product = productRepository.findById(id).orElseThrow();

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nome do Produto obrigatório");
        }

        PriceValidator.validate(newPrice);
        BarCodeValidator.validate(newBarCode);

        if (productRepository.existsByBarCode(newBarCode) && !BarCodeValidator.compare(product.getBarCode(), newBarCode)) {
            throw new IllegalArgumentException("Já existe um produto com esse código de barras");
        }

        product.setName(newName);
        product.setBarCode(!BarCodeValidator.isEmptyOrNull(newBarCode) ? newBarCode : null);
        product.setPrice(PriceValidator.parsePrice(newPrice));

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
