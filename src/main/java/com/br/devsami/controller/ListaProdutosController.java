package com.br.devsami.controller;


import com.br.devsami.view.components.ProductCell;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.service.ProductService;
import javafx.stage.Stage;

public class ListaProdutosController implements Initializable {

    @FXML
    private Pagination paginacao;

    private final int pageSize = 10;
    private final ProductService productService = new ProductService();

    @Override
        public void initialize(URL location, ResourceBundle resources) {
        // 1. Defina o total de páginas buscando o COUNT do seu BD
        paginacao.setPageCount(productService.countPages(pageSize));

        // 2. Configura o método que será chamado ao trocar de página
        paginacao.setPageFactory(this::criarPaginaDaLista);
    }

    // Este método retorna o conteúdo visual de uma página específica
    private Node criarPaginaDaLista(int indicePagina) {
        ListView<Product> listView = new ListView<>();

        List<Product> itensDoBanco = productService.listProducts(indicePagina, pageSize);
        listView.getItems().addAll(itensDoBanco);

        // Configura a fábrica de células, passando a referência do próprio Controller
        listView.setCellFactory(param -> new ProductCell(this));

        return listView;
    }

    public void recarregarLista() {
        int paginaAtual = paginacao.getCurrentPageIndex();

        //Busca no banco se o total de páginas mudou
        int novoTotalDePaginas = productService.countPages(pageSize);
        paginacao.setPageCount(novoTotalDePaginas);

        //Definir a PageFactory novamente força a paginação a recarregar
        paginacao.setPageFactory(this::criarPaginaDaLista);

        //Volta para a página que o usuário estava
        if (paginaAtual >= novoTotalDePaginas && novoTotalDePaginas > 0) {
            paginacao.setCurrentPageIndex(novoTotalDePaginas - 1);
        } else {
            paginacao.setCurrentPageIndex(paginaAtual);
        }
    }

    @FXML
    void handleAdicionar(ActionEvent event) {
        abrirFormulario(event, null);
    }

    // --- Métodos que a Célula vai chamar ---

    public void editarItem(ActionEvent event, Product product) {
        abrirFormulario(event, product);
    }

    public void removerItem(Product product) {
        productService.deleteProduct(product.getId());
        recarregarLista();
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirFormulario(ActionEvent event, Product produtoSelecionado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GerenciaProduto.fxml"));
            Parent root = loader.load();

            GerenciaProdutoController formController = loader.getController();
            formController.setProduct(produtoSelecionado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle(produtoSelecionado == null ? "Novo Produto" : "Editar Produto");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
