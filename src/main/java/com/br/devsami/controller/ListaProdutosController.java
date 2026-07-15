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
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.service.ProductService;


public class ListaProdutosController implements Initializable {

    @FXML public FontIcon iconSortByBtn;
    @FXML public FontIcon iconSortOrderBtn;
    @FXML private Pagination paginacao;

    private final int pageSize = 10;
    private final ProductService productService = new ProductService();
    private String sortBy;
    private boolean sortDesc;

    @Override
        public void initialize(URL location, ResourceBundle resources) {
        sortBy = "NAME";
        sortDesc = false;

        carregarLista();
    }


    // Este método retorna o conteúdo visual de uma página específica
    private Node criarPaginaDaLista(int indicePagina) {
        ListView<Product> listView = new ListView<>();
        listView.setCellFactory(param -> new ProductCell(this));
        listView.setPlaceholder(new Label("Carregando produtos..."));

        // Busca no banco de dados numa thread separada
        CompletableFuture.supplyAsync(() -> productService.listProducts(indicePagina, pageSize, sortBy, sortDesc)).thenAccept(listaProdutos -> {
            Platform.runLater(() -> {
                listView.getItems().clear();
                listView.getItems().addAll(listaProdutos);
            });

        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                listView.setPlaceholder(new Label("Erro ao carregar os dados."));
                ex.printStackTrace();
            });
            return null;
        });

        return listView;
    }

    public void carregarLista() {
        int paginaAtual = paginacao.getCurrentPageIndex();

        // Busca no banco a quantidade de páginas mudou
        CompletableFuture.supplyAsync(() -> {
            return productService.countPages(pageSize);

        }).thenAccept(novoTotalDePaginas -> {
            Platform.runLater(() -> {
                paginacao.setPageCount(novoTotalDePaginas);

                // Define o metódo que será chamado ao mudar de página, forçando a view a reiniciar
                paginacao.setPageFactory(this::criarPaginaDaLista);

                if (paginaAtual >= novoTotalDePaginas && novoTotalDePaginas > 0) {
                    paginacao.setCurrentPageIndex(novoTotalDePaginas - 1);
                } else {
                    paginacao.setCurrentPageIndex(paginaAtual);
                }
            });

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void handleSortBy(){
        if (sortBy.equalsIgnoreCase("NAME")){
            sortBy = "PRICE";
            iconSortByBtn.setIconLiteral("mdi-sort-numeric");
        }
        else {
            sortBy = "NAME";
            iconSortByBtn.setIconLiteral("mdi-sort-alphabetical");
        }

        carregarLista();
    }

    public void handleSortOrder(){
        sortDesc = !sortDesc;
        if(sortDesc){
            iconSortOrderBtn.setIconLiteral("mdi-sort-descending");
        }else{
            iconSortOrderBtn.setIconLiteral("mdi-sort-ascending");
        }

        carregarLista();
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
        CompletableFuture.runAsync(() -> {
            productService.deleteProduct(product.getId());

        }).thenRun(() -> {
            Platform.runLater(this::carregarLista);

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
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
