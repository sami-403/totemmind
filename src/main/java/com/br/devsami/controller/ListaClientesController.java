package com.br.devsami.controller;


import com.br.devsami.model.entity.User;
import com.br.devsami.model.service.UserService;
import com.br.devsami.view.components.UserCell;
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
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;



public class ListaClientesController implements Initializable {

    @FXML public FontIcon iconSortOrderBtn;
    @FXML public Button searchTryButton;
    public Button btnAdicionar;
    @FXML private Pagination paginacao;
    @FXML private TextField CPFSearch;

    private final int pageSize = 10;
    private final UserService userService = new UserService();
    private boolean sortDesc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortDesc = false;

        carregarLista();
    }


    // Este método retorna o conteúdo visual de uma página específica
    private Node criarPaginaDaLista(int indicePagina) {
        ListView<User> listView = new ListView<>();
        listView.setCellFactory(param -> new UserCell(this));
        listView.setPlaceholder(new Label("Carregando clientes..."));

        // Busca no banco de dados numa thread separada
        CompletableFuture.supplyAsync(() -> userService.listUsers(indicePagina, pageSize, sortDesc)).thenAccept(listaClientes -> {
            Platform.runLater(() -> {
                listView.getItems().clear();
                listView.getItems().addAll(listaClientes);
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
            return userService.countPages(pageSize);

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

    @FXML
    void handleSearchButton(ActionEvent event){
        String cpf = CPFSearch.getText();

        try{
            User foundUser = userService.findByCpf(cpf).orElseThrow();
            abrirFormulario(event, foundUser);
        }catch (IllegalArgumentException e){
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", e.getMessage());
        }
        catch (NoSuchElementException e){
            showAlert(Alert.AlertType.WARNING, "Usuário Não Encontrado", e.getMessage());
        }

    }

    // --- Métodos que a Célula vai chamar ---
    public void editarItem(ActionEvent event, User user) {
        abrirFormulario(event, user);
    }

    public void removerItem(User user) {
        CompletableFuture.runAsync(() -> {
//            userService.(user.getId());

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
    public void abrirFormulario(ActionEvent event, User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CadastroCliente.fxml"));
            Parent root = loader.load();

            CadastroClienteController formController = loader.getController();
            formController.setCpf(selectedUser.getCpf());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
