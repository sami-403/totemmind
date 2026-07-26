package com.br.devsami.controller;

import com.br.devsami.model.entity.*;
import com.br.devsami.model.enums.*;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.model.repository.ProductRepository;
import com.br.devsami.model.repository.UserRepository;
import com.br.devsami.model.service.FeedbackService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TelaGerenciaFeedbackController {

    @FXML
    private ComboBox<String> cbFilterTipo;
    @FXML
    private ComboBox<String> cbFilterSentimento;
    @FXML
    private TextField txtBusca;

    @FXML
    private TableView<Feedback> tvFeedbacks;
    @FXML
    private TableColumn<Feedback, String> colData;
    @FXML
    private TableColumn<Feedback, String> colTipo;
    @FXML
    private TableColumn<Feedback, String> colAlvo;
    @FXML
    private TableColumn<Feedback, String> colCliente;
    @FXML
    private TableColumn<Feedback, String> colSentimento;

    @FXML
    private VBox formContainer;
    @FXML
    private Label formTitle;
    @FXML
    private VBox boxTipoFeedback;
    @FXML
    private ComboBox<String> cbTipoFeedback;
    @FXML
    private DatePicker dpData;
    @FXML
    private TextField txtHora;
    @FXML
    private Label lblAlvoTitle;
    @FXML
    private ComboBox<UserDisplayItem> cbCliente;
    @FXML
    private ComboBox<AlvoDisplayItem> cbAlvo;
    @FXML
    private ComboBox<Feeling> cbFeeling;
    @FXML
    private ComboBox<CategoryDisplayItem> cbCategory;
    @FXML
    private VBox boxRating;
    @FXML
    private ComboBox<Integer> cbRating;
    @FXML
    private TextArea txtComment;
    @FXML
    private Button actionButton;

    private final FeedbackService feedbackService = new FeedbackService();
    private final UserRepository userRepository = new UserRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final EmployeeRepository employeeRepository = new EmployeeRepository();

    private final List<Feedback> todosFeedbacks = new ArrayList<>();
    private final List<UserDisplayItem> todosClientes = new ArrayList<>();
    private final List<AlvoDisplayItem> todosProdutos = new ArrayList<>();
    private final List<AlvoDisplayItem> todosFuncionarios = new ArrayList<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private String currentMode = "";
    private Feedback currentSelectedFeedback = null;

    // Wrapper Classes para os ComboBoxes com exibição limpa
    public record UserDisplayItem(User user) {
        @Override
        public String toString() {
            if (user == null) return "Anônimo";
            return user.getName() + (user.getCpf() != null ? " (" + user.getCpf() + ")" : "");
        }
    }

    public record AlvoDisplayItem(Object target) {
        @Override
        public String toString() {
            if (target instanceof Product p) {
                return p.getName() + String.format(" (R$ %.2f)", p.getPrice());
            } else if (target instanceof Employee e) {
                return e.getName() + " (" + e.getTipo() + ")";
            }
            return "Indefinido";
        }
    }

    public record CategoryDisplayItem(String enumName, String description) {
        @Override
        public String toString() {
            return description;
        }
    }

    @FXML
    public void initialize() {
        // Inicializa filtros da barra superior
        cbFilterTipo.getItems().setAll("Todos", "Produto", "Atendimento");
        cbFilterTipo.setValue("Todos");
        cbFilterTipo.setOnAction(e -> aplicarFiltros());

        cbFilterSentimento.getItems().setAll("Todos", "Satisfeito", "Neutro", "Insatisfeito");
        cbFilterSentimento.setValue("Todos");
        cbFilterSentimento.setOnAction(e -> aplicarFiltros());

        txtBusca.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // Tipo de feedback para modo de adição
        cbTipoFeedback.getItems().setAll("Produto", "Atendimento");
        cbTipoFeedback.setOnAction(e -> alternarTipoFeedbackNovo(cbTipoFeedback.getValue()));

        // Opções básicas
        cbFeeling.getItems().setAll(Feeling.values());
        cbRating.getItems().setAll(1, 2, 3, 4, 5);

        // Configura colunas da TableView
        colData.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getCreatedAt() != null ? cell.getValue().getCreatedAt().format(formatter) : ""));

        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() instanceof ProductFeedback ? "📦 PRODUTO" : "👤 ATENDIMENTO"));

        colAlvo.setCellValueFactory(cell -> {
            Feedback f = cell.getValue();
            if (f instanceof ProductFeedback pf) {
                return new SimpleStringProperty(pf.getProduct() != null ? pf.getProduct().getName() : "Produto Excluído");
            } else if (f instanceof EmployeeFeedback ef) {
                return new SimpleStringProperty(ef.getEmployee() != null ? ef.getEmployee().getName() : "Funcionário Inativo");
            }
            return new SimpleStringProperty("-");
        });

        colCliente.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getUser() != null ? cell.getValue().getUser().getName() : "Anônimo"));

        colSentimento.setCellValueFactory(cell -> {
            Feedback f = cell.getValue();
            String feelingStr = f.getFeeling() != null ? f.getFeeling().name() : "N/A";
            if (f instanceof ProductFeedback pf && pf.getRating() != null) {
                feelingStr += String.format(" (%d ⭐)", pf.getRating());
            }
            return new SimpleStringProperty(feelingStr);
        });

        // Listener ao selecionar um feedback na tabela
        tvFeedbacks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                currentSelectedFeedback = newSel;
                if (formContainer.isVisible() && !"ADD".equals(currentMode)) {
                    preencherFormulario(newSel);
                }
            }
        });

        carregarDadosAuxiliares();
        carregarFeedbacks();
    }

    private void carregarDadosAuxiliares() {
        CompletableFuture.runAsync(() -> {
            List<User> users = userRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Employee> employees = employeeRepository.findAllActive();

            Platform.runLater(() -> {
                todosClientes.clear();
                users.forEach(u -> todosClientes.add(new UserDisplayItem(u)));
                cbCliente.getItems().setAll(todosClientes);

                todosProdutos.clear();
                products.forEach(p -> todosProdutos.add(new AlvoDisplayItem(p)));

                todosFuncionarios.clear();
                employees.forEach(e -> todosFuncionarios.add(new AlvoDisplayItem(e)));
            });
        });
    }

    private void carregarFeedbacks() {
        CompletableFuture.supplyAsync(feedbackService::findAllFeedbacks)
                .thenAccept(lista -> Platform.runLater(() -> {
                    todosFeedbacks.clear();
                    todosFeedbacks.addAll(lista);
                    aplicarFiltros();
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void aplicarFiltros() {
        String tipoFiltro = cbFilterTipo.getValue();
        String sentimentoFiltro = cbFilterSentimento.getValue();
        String busca = txtBusca.getText() != null ? txtBusca.getText().toLowerCase().trim() : "";

        List<Feedback> filtrados = todosFeedbacks.stream().filter(f -> {
            // Filtro por Tipo
            if ("Produto".equals(tipoFiltro) && !(f instanceof ProductFeedback)) return false;
            if ("Atendimento".equals(tipoFiltro) && !(f instanceof EmployeeFeedback)) return false;

            // Filtro por Sentimento
            if ("Satisfeito".equals(sentimentoFiltro) && f.getFeeling() != Feeling.SATISFIED) return false;
            if ("Neutro".equals(sentimentoFiltro) && f.getFeeling() != Feeling.NEUTRAL) return false;
            if ("Insatisfeito".equals(sentimentoFiltro) && f.getFeeling() != Feeling.DISSATISFIED) return false;

            // Filtro por Texto de Busca
            if (!busca.isEmpty()) {
                String cliente = f.getUser() != null ? f.getUser().getName().toLowerCase() : "";
                String texto = f.getText() != null ? f.getText().toLowerCase() : "";
                String alvo = "";
                if (f instanceof ProductFeedback pf && pf.getProduct() != null) {
                    alvo = pf.getProduct().getName().toLowerCase();
                } else if (f instanceof EmployeeFeedback ef && ef.getEmployee() != null) {
                    alvo = ef.getEmployee().getName().toLowerCase();
                }
                return cliente.contains(busca) || texto.contains(busca) || alvo.contains(busca);
            }

            return true;
        }).toList();

        tvFeedbacks.getItems().setAll(filtrados);
    }

    @FXML
    void voltar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/TelaGerenciaSeletor.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setModeAdd() {
        this.currentMode = "ADD";
        this.currentSelectedFeedback = null;
        configureForm("Adicionar Novo Feedback", "#1A9347", "Cadastrar Feedback");

        boxTipoFeedback.setVisible(true);
        boxTipoFeedback.setManaged(true);

        cbTipoFeedback.setValue("Produto");
        alternarTipoFeedbackNovo("Produto");

        cbCliente.setValue(null);
        cbFeeling.setValue(Feeling.SATISFIED);
        txtComment.clear();

        dpData.setValue(LocalDate.now());
        txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    public void setModeEdit() {
        Feedback selected = tvFeedbacks.getSelectionModel().getSelectedItem();
        if (selected == null && currentSelectedFeedback != null) {
            selected = currentSelectedFeedback;
        }
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Necessária", "Por favor, selecione um feedback na tabela para editar.");
            return;
        }
        this.currentSelectedFeedback = selected;
        this.currentMode = "EDIT";
        boxTipoFeedback.setVisible(false);
        boxTipoFeedback.setManaged(false);

        configureForm("Editar Feedback", "#1A9347", "Salvar Alterações");
        preencherFormulario(selected);
    }

    @FXML
    public void setModeRemove() {
        Feedback selected = tvFeedbacks.getSelectionModel().getSelectedItem();
        if (selected == null && currentSelectedFeedback != null) {
            selected = currentSelectedFeedback;
        }
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Necessária", "Por favor, selecione um feedback na tabela para remover.");
            return;
        }

        // Não abre o formulário de digitação para exclusão
        formContainer.setVisible(false);
        formContainer.setManaged(false);

        String cliente = selected.getUser() != null ? selected.getUser().getName() : "Anônimo";
        String alvo = "-";
        if (selected instanceof ProductFeedback pf && pf.getProduct() != null) {
            alvo = pf.getProduct().getName();
        } else if (selected instanceof EmployeeFeedback ef && ef.getEmployee() != null) {
            alvo = ef.getEmployee().getName();
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir Feedback");
        confirm.setContentText(String.format("Deseja realmente excluir o feedback de '%s' (Alvo: %s) permanentemente?", cliente, alvo));

        final Feedback finalSelected = selected;
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                feedbackService.deleteFeedback(finalSelected);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Feedback excluído!");
                currentSelectedFeedback = null;
                carregarFeedbacks();
            }
        });
    }

    private void configureForm(String title, String btnColor, String btnText) {
        formContainer.setVisible(true);
        formContainer.setManaged(true);
        formTitle.setText(title);

        actionButton.setText(btnText);
        actionButton.setStyle("-fx-background-color: " + btnColor
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 6px; -fx-cursor: hand;");
    }

    private void alternarTipoFeedbackNovo(String tipo) {
        cbCategory.getItems().clear();
        cbAlvo.getItems().clear();

        if ("Produto".equals(tipo)) {
            lblAlvoTitle.setText("Alvo (Produto):");
            cbAlvo.getItems().setAll(todosProdutos);
            boxRating.setVisible(true);
            boxRating.setManaged(true);
            cbRating.setValue(5);

            for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                cbCategory.getItems().add(new CategoryDisplayItem(cat.name(), cat.getDescription()));
            }
        } else {
            lblAlvoTitle.setText("Alvo (Funcionário):");
            cbAlvo.getItems().setAll(todosFuncionarios);
            boxRating.setVisible(false);
            boxRating.setManaged(false);

            for (EmployeeFeedbackCategory cat : EmployeeFeedbackCategory.values()) {
                cbCategory.getItems().add(new CategoryDisplayItem(cat.name(), cat.getDescription()));
            }
        }
    }

    private void preencherFormulario(Feedback f) {
        if (f == null) return;

        // Data e hora do registro
        if (f.getCreatedAt() != null) {
            dpData.setValue(f.getCreatedAt().toLocalDate());
            txtHora.setText(f.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            dpData.setValue(LocalDate.now());
            txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // Seleciona Cliente / Autor
        if (f.getUser() != null) {
            cbCliente.getItems().stream()
                    .filter(item -> item.user() != null && item.user().getId().equals(f.getUser().getId()))
                    .findFirst()
                    .ifPresent(cbCliente::setValue);
        } else {
            cbCliente.setValue(null);
        }

        cbFeeling.setValue(f.getFeeling());
        txtComment.setText(f.getText());

        cbCategory.getItems().clear();
        cbAlvo.getItems().clear();

        if (f instanceof ProductFeedback pf) {
            lblAlvoTitle.setText("Alvo (Produto):");
            cbAlvo.getItems().setAll(todosProdutos);
            if (pf.getProduct() != null) {
                todosProdutos.stream()
                        .filter(item -> item.target() instanceof Product p && p.getId().equals(pf.getProduct().getId()))
                        .findFirst()
                        .ifPresent(cbAlvo::setValue);
            }

            boxRating.setVisible(true);
            boxRating.setManaged(true);
            cbRating.setValue(pf.getRating());

            for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                cbCategory.getItems().add(new CategoryDisplayItem(cat.name(), cat.getDescription()));
            }
            if (pf.getProductCategory() != null) {
                cbCategory.getItems().stream()
                        .filter(c -> c.enumName().equals(pf.getProductCategory().name()))
                        .findFirst()
                        .ifPresent(cbCategory::setValue);
            }
        } else if (f instanceof EmployeeFeedback ef) {
            lblAlvoTitle.setText("Alvo (Funcionário):");
            cbAlvo.getItems().setAll(todosFuncionarios);
            if (ef.getEmployee() != null) {
                todosFuncionarios.stream()
                        .filter(item -> item.target() instanceof Employee emp && Objects.equals(emp.getId(), ef.getEmployee().getId()))
                        .findFirst()
                        .ifPresent(cbAlvo::setValue);
            }

            boxRating.setVisible(false);
            boxRating.setManaged(false);

            for (EmployeeFeedbackCategory cat : EmployeeFeedbackCategory.values()) {
                cbCategory.getItems().add(new CategoryDisplayItem(cat.name(), cat.getDescription()));
            }
            if (ef.getEmployeeCategory() != null) {
                cbCategory.getItems().stream()
                        .filter(c -> c.enumName().equals(ef.getEmployeeCategory().name()))
                        .findFirst()
                        .ifPresent(cbCategory::setValue);
            }
        }
    }

    private LocalDateTime extrairDataHoraFormulario() {
        LocalDate date = dpData.getValue() != null ? dpData.getValue() : LocalDate.now();
        LocalTime time = LocalTime.now();
        String horaText = txtHora.getText() != null ? txtHora.getText().trim() : "";
        if (!horaText.isEmpty()) {
            try {
                time = LocalTime.parse(horaText, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                try {
                    time = LocalTime.parse(horaText + ":00");
                } catch (Exception ignored2) {}
            }
        }
        return LocalDateTime.of(date, time);
    }

    @FXML
    private void handleAction() {
        UserDisplayItem chosenUserItem = cbCliente.getValue();
        if (chosenUserItem == null || chosenUserItem.user() == null) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Por favor, selecione um Cliente/Autor.");
            return;
        }

        AlvoDisplayItem chosenAlvoItem = cbAlvo.getValue();
        if (chosenAlvoItem == null || chosenAlvoItem.target() == null) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Por favor, selecione um Alvo (Produto ou Funcionário).");
            return;
        }

        LocalDateTime dataHora = extrairDataHoraFormulario();
        CategoryDisplayItem selectedCat = cbCategory.getValue();
        String enumName = selectedCat != null ? selectedCat.enumName() : null;

        try {
            if ("ADD".equals(currentMode)) {
                String tipo = cbTipoFeedback.getValue();
                if ("Produto".equals(tipo)) {
                    if (!(chosenAlvoItem.target() instanceof Product p)) {
                        showAlert(Alert.AlertType.WARNING, "Validação", "O alvo selecionado deve ser um produto.");
                        return;
                    }
                    ProductFeedback pf = new ProductFeedback();
                    pf.setUser(chosenUserItem.user());
                    pf.setProduct(p);
                    pf.setFeeling(cbFeeling.getValue() != null ? cbFeeling.getValue() : Feeling.SATISFIED);
                    pf.setRating(cbRating.getValue() != null ? cbRating.getValue() : 5);
                    pf.setText(txtComment.getText());
                    pf.setCreatedAt(dataHora);

                    if (enumName != null) {
                        try { pf.setProductCategory(ProductFeedbackCategory.valueOf(enumName)); } catch (Exception ignored) {}
                        try { pf.setCategory(FeedbackCategory.valueOf(enumName)); } catch (Exception ignored) {}
                    }

                    feedbackService.createFeedback(pf);
                } else {
                    if (!(chosenAlvoItem.target() instanceof Employee emp)) {
                        showAlert(Alert.AlertType.WARNING, "Validação", "O alvo selecionado deve ser um funcionário.");
                        return;
                    }
                    EmployeeFeedback ef = new EmployeeFeedback();
                    ef.setUser(chosenUserItem.user());
                    ef.setEmployee(emp);
                    ef.setFeeling(cbFeeling.getValue() != null ? cbFeeling.getValue() : Feeling.SATISFIED);
                    ef.setText(txtComment.getText());
                    ef.setCreatedAt(dataHora);

                    if (enumName != null) {
                        try { ef.setEmployeeCategory(EmployeeFeedbackCategory.valueOf(enumName)); } catch (Exception ignored) {}
                        try { ef.setCategory(FeedbackCategory.valueOf(enumName)); } catch (Exception ignored) {}
                    }

                    feedbackService.createFeedback(ef);
                }

                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Novo feedback cadastrado com sucesso!");
                formContainer.setVisible(false);
                formContainer.setManaged(false);
                carregarFeedbacks();
            } else if ("EDIT".equals(currentMode)) {
                Feedback selected = currentSelectedFeedback != null ? currentSelectedFeedback : tvFeedbacks.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showAlert(Alert.AlertType.WARNING, "Seleção Necessária", "Nenhum feedback selecionado.");
                    return;
                }

                selected.setUser(chosenUserItem.user());
                selected.setFeeling(cbFeeling.getValue());
                selected.setText(txtComment.getText());
                selected.setCreatedAt(dataHora);

                if (enumName != null) {
                    try { selected.setCategory(FeedbackCategory.valueOf(enumName)); } catch (IllegalArgumentException ignored) {}
                }

                if (selected instanceof ProductFeedback pf) {
                    pf.setRating(cbRating.getValue());
                    if (chosenAlvoItem.target() instanceof Product p) {
                        pf.setProduct(p);
                    }
                    if (enumName != null) {
                        try { pf.setProductCategory(ProductFeedbackCategory.valueOf(enumName)); } catch (IllegalArgumentException ignored) {}
                    }
                } else if (selected instanceof EmployeeFeedback ef) {
                    if (chosenAlvoItem.target() instanceof Employee emp) {
                        ef.setEmployee(emp);
                    }
                    if (enumName != null) {
                        try { ef.setEmployeeCategory(EmployeeFeedbackCategory.valueOf(enumName)); } catch (IllegalArgumentException ignored) {}
                    }
                }

                feedbackService.updateFeedback(selected);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Feedback atualizado com sucesso!");
                carregarFeedbacks();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", e.getMessage());
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
