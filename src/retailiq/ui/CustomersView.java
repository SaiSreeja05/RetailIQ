package retailiq.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.Customer;
import retailiq.Product;
import retailiq.Review;
import retailiq.Transaction;
import retailiq.csv.DataValidator;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomersView extends VBox {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final TableView<Customer> table = new TableView<>();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    private final VBox profilePane = new VBox(12);

    public CustomersView(RetailIQService service) {
        this.service = service;
        this.analytics = new AnalyticsEngine(service);

        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("main-container");

        buildHeader();
        buildContent();

        refresh();
        service.addChangeListener(this::refresh);
    }

    private void buildHeader() {
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Customer Directory & Account Profiles");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Manage customer relationships, view order history, and analyze customer lifetime value");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add New Customer");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showCustomerDialog(null));

        topRow.getChildren().addAll(titleBox, btnAdd);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search customer by name, email, phone, city, or ID...");
        searchField.setPrefWidth(340);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        filterBar.getChildren().add(searchField);
        getChildren().addAll(topRow, filterBar);
    }

    private void buildContent() {
        HBox split = new HBox(16);
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("card");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        buildTableColumns();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableBox.getChildren().add(table);

        profilePane.getStyleClass().add("card");
        profilePane.setPrefWidth(360);
        profilePane.setMinWidth(320);
        renderEmptyProfile();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                renderCustomerProfile(newV);
            } else {
                renderEmptyProfile();
            }
        });

        split.getChildren().addAll(tableBox, profilePane);
        getChildren().add(split);
    }

    private void buildTableColumns() {
        TableColumn<Customer, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCustomerId()));
        colId.setPrefWidth(60);

        TableColumn<Customer, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(140);

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email Address");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colEmail.setPrefWidth(180);

        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone Number");
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colPhone.setPrefWidth(120);

        TableColumn<Customer, String> colAddr = new TableColumn<>("City / Address");
        colAddr.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        colAddr.setPrefWidth(140);

        TableColumn<Customer, Customer> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<Customer, Customer>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDel = new Button("Delete");
            private final HBox pane = new HBox(6, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Customer c = getItem();
                    if (c != null) showCustomerDialog(c);
                });
                btnDel.setOnAction(e -> {
                    Customer c = getItem();
                    if (c != null) handleDeleteCustomer(c);
                });
            }

            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        colActions.setPrefWidth(140);

        table.getColumns().addAll(colId, colName, colEmail, colPhone, colAddr, colActions);
    }

    private void renderEmptyProfile() {
        profilePane.getChildren().clear();
        Label title = new Label("Customer Profile");
        title.getStyleClass().add("card-header");
        Label prompt = new Label("Select a customer from the table to view their lifetime spend, order history, and feedback.");
        prompt.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        prompt.setWrapText(true);
        profilePane.getChildren().addAll(title, prompt);
    }

    private void renderCustomerProfile(Customer c) {
        profilePane.getChildren().clear();

        Label title = new Label("Customer Profile");
        title.getStyleClass().add("card-header");

        Label name = new Label(c.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label idLabel = new Label("Customer ID: #" + c.getCustomerId());
        idLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        VBox contactBox = new VBox(4);
        contactBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 10px; -fx-background-radius: 8px;");
        contactBox.getChildren().addAll(
                new Label("📧 " + c.getEmail()),
                new Label("📞 " + c.getPhone()),
                new Label("📍 " + c.getAddress())
        );
        for (javafx.scene.Node n : contactBox.getChildren()) {
            n.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        }

        Separator sep1 = new Separator();

        // Spend & Stats
        List<Transaction> txns = analytics.getCustomerTransactions(c.getCustomerId());
        double totalSpend = analytics.getCustomerTotalSpend(c.getCustomerId());

        HBox spendBox = new HBox(12);
        spendBox.setAlignment(Pos.CENTER_LEFT);
        VBox spendCard = UIHelper.createKpiCard("LIFETIME VALUE", UIHelper.formatCurrency(totalSpend), txns.size() + " orders placed", "emerald");
        spendBox.getChildren().add(spendCard);

        Separator sep2 = new Separator();

        // Transaction History Table
        Label txnHeader = new Label("Order History (" + txns.size() + ")");
        txnHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc; -fx-font-size: 13px;");

        VBox txnList = new VBox(6);
        if (txns.isEmpty()) {
            Label noTxn = new Label("No transaction records found.");
            noTxn.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            txnList.getChildren().add(noTxn);
        } else {
            for (Transaction t : txns) {
                HBox row = new HBox(8);
                row.setStyle("-fx-background-color: #0f172a; -fx-padding: 6px 10px; -fx-background-radius: 6px;");
                row.setAlignment(Pos.CENTER_LEFT);

                Label lblId = new Label(t.getTransactionId());
                lblId.setStyle("-fx-font-weight: bold; -fx-text-fill: #60a5fa; -fx-font-size: 11.5px;");

                Label lblDate = new Label(t.getTransactionDate());
                lblDate.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblAmt = new Label(UIHelper.formatCurrency(t.getTotalAmount()));
                lblAmt.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399; -fx-font-size: 11.5px;");

                row.getChildren().addAll(lblId, lblDate, spacer, lblAmt);
                txnList.getChildren().add(row);
            }
        }

        ScrollPane scrollTxns = new ScrollPane(txnList);
        scrollTxns.setFitToWidth(true);
        scrollTxns.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollTxns.setPrefHeight(130);

        profilePane.getChildren().addAll(title, name, idLabel, contactBox, sep1, spendBox, sep2, txnHeader, scrollTxns);
    }

    private void handleDeleteCustomer(Customer c) {
        boolean confirm = UIHelper.showConfirm("Delete Customer", "Delete " + c.getName() + "?",
                "Are you sure you want to delete Customer #" + c.getCustomerId() + "? This action persists directly to customers.csv.");
        if (confirm) {
            service.deleteCustomer(c.getCustomerId());
        }
    }

    private void showCustomerDialog(Customer existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Customer" : "Edit Customer #" + existing.getCustomerId());
        dialog.setHeaderText(existing == null ? "Register a new customer account" : "Modify customer information");

        ButtonType btnSaveType = new ButtonType("Save to CSV", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField tfId = new TextField(existing != null ? String.valueOf(existing.getCustomerId()) : String.valueOf(getNextId()));
        if (existing != null) tfId.setDisable(true);
        TextField tfName = new TextField(existing != null ? existing.getName() : "");
        TextField tfEmail = new TextField(existing != null ? existing.getEmail() : "");
        TextField tfPhone = new TextField(existing != null ? existing.getPhone() : "");
        TextField tfAddress = new TextField(existing != null ? existing.getAddress() : "");

        grid.add(new Label("Customer ID:"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1); grid.add(tfName, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(tfEmail, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(tfPhone, 1, 3);
        grid.add(new Label("Address / City:"), 0, 4); grid.add(tfAddress, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String name = tfName.getText().trim();
                String email = tfEmail.getText().trim();
                String phone = tfPhone.getText().trim();
                String address = tfAddress.getText().trim();

                Customer c = new Customer(id, name, email, phone, address);
                DataValidator.ValidationResult val = service.saveCustomer(c);
                if (!val.isValid()) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Customer Data", val.getErrorMessage());
                }
            } catch (Exception ex) {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Input Error", "Failed to parse inputs", ex.getMessage());
            }
        }
    }

    private int getNextId() {
        return service.getAllCustomers().stream().mapToInt(Customer::getCustomerId).max().orElse(200) + 1;
    }

    public void refresh() {
        masterData.setAll(service.getAllCustomers());
        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        List<Customer> filtered = masterData.stream().filter(c -> {
            if (q.isEmpty()) return true;
            return c.getName().toLowerCase().contains(q)
                    || c.getEmail().toLowerCase().contains(q)
                    || c.getPhone().contains(q)
                    || c.getAddress().toLowerCase().contains(q)
                    || String.valueOf(c.getCustomerId()).contains(q);
        }).collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }
}
