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
import retailiq.Transaction;
import retailiq.csv.DataValidator;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionsView extends VBox {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final TableView<Transaction> table = new TableView<>();
    private final ObservableList<Transaction> masterData = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    public TransactionsView(RetailIQService service) {
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
        Label title = new Label("Sales Ledger & Transaction Records");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Track real-time orders, calculate revenues, inspect checkout baskets, and manage persistent ledger entries");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnAdd = new Button("+ Record New Transaction");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showRecordTransactionDialog());

        topRow.getChildren().addAll(titleBox, btnAdd);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Filter by Transaction ID, Customer ID, Product ID, or Date (YYYY-MM-DD)...");
        searchField.setPrefWidth(420);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        filterBar.getChildren().add(searchField);
        getChildren().addAll(topRow, filterBar);
    }

    private void buildContent() {
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("card");
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        buildTableColumns();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableBox.getChildren().add(table);

        getChildren().add(tableBox);
    }

    private void buildTableColumns() {
        TableColumn<Transaction, String> colId = new TableColumn<>("Txn ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionId()));
        colId.setPrefWidth(110);

        TableColumn<Transaction, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionDate()));
        colDate.setPrefWidth(100);

        TableColumn<Transaction, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(d -> {
            int cId = d.getValue().getCustomerId();
            Optional<Customer> opt = service.getCustomerRepo().findById(cId);
            String name = opt.map(Customer::getName).orElse("Unknown");
            return new SimpleStringProperty(name + " (#" + cId + ")");
        });
        colCustomer.setPrefWidth(160);

        TableColumn<Transaction, String> colProduct = new TableColumn<>("Purchased Product");
        colProduct.setCellValueFactory(d -> {
            int pId = d.getValue().getProductId();
            Optional<Product> opt = service.getProductRepo().findById(pId);
            String name = opt.map(Product::getName).orElse("Unknown Product");
            return new SimpleStringProperty(name + " (#" + pId + ")");
        });
        colProduct.setPrefWidth(220);

        TableColumn<Transaction, Number> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantity()));
        colQty.setPrefWidth(80);

        TableColumn<Transaction, String> colAmount = new TableColumn<>("Total Amount");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.formatCurrency(d.getValue().getTotalAmount())));
        colAmount.setPrefWidth(130);

        TableColumn<Transaction, Transaction> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<Transaction, Transaction>() {
            private final Button btnDel = new Button("Delete");

            {
                btnDel.getStyleClass().add("btn-danger");
                btnDel.setOnAction(e -> {
                    Transaction t = getItem();
                    if (t != null) handleDeleteTransaction(t);
                });
            }

            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(btnDel);
                    pane.setAlignment(Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });
        colActions.setPrefWidth(100);

        table.getColumns().addAll(colId, colDate, colCustomer, colProduct, colQty, colAmount, colActions);
    }

    private void handleDeleteTransaction(Transaction t) {
        boolean confirm = UIHelper.showConfirm("Delete Transaction", "Delete Transaction " + t.getTransactionId() + "?",
                "Are you sure you want to remove Transaction " + t.getTransactionId() + "? This action persists directly to transactions.csv.");
        if (confirm) {
            service.deleteTransaction(t.getTransactionId());
        }
    }

    private void showRecordTransactionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record New Retail Transaction");
        dialog.setHeaderText("Create a transaction, adjust product inventory, and write to CSV ledger");

        ButtonType btnSaveType = new ButtonType("Save to CSV", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField tfId = new TextField("TXN" + (1000 + service.getAllTransactions().size() + 1));

        ComboBox<Customer> cbCustomer = new ComboBox<>();
        cbCustomer.setItems(FXCollections.observableArrayList(service.getAllCustomers()));
        cbCustomer.setCellFactory(lv -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (#" + item.getCustomerId() + ")");
            }
        });
        cbCustomer.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (#" + item.getCustomerId() + ")");
            }
        });
        if (!cbCustomer.getItems().isEmpty()) cbCustomer.getSelectionModel().selectFirst();

        ComboBox<Product> cbProduct = new ComboBox<>();
        cbProduct.setItems(FXCollections.observableArrayList(service.getAllProducts()));
        cbProduct.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (Rs. " + (int)item.getPrice() + " | Stock: " + item.getStock() + ")");
            }
        });
        cbProduct.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (Rs. " + (int)item.getPrice() + " | Stock: " + item.getStock() + ")");
            }
        });
        if (!cbProduct.getItems().isEmpty()) cbProduct.getSelectionModel().selectFirst();

        Spinner<Integer> spQty = new Spinner<>(1, 100, 1);
        spQty.setEditable(true);

        Label lblTotal = new Label("Rs. 0.00");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399; -fx-font-size: 14px;");

        Runnable calcTotal = () -> {
            Product p = cbProduct.getValue();
            int qty = spQty.getValue();
            if (p != null) {
                double total = p.getPrice() * qty;
                lblTotal.setText(UIHelper.formatCurrency(total));
            }
        };

        cbProduct.setOnAction(e -> calcTotal.run());
        spQty.valueProperty().addListener((obs, oldV, newV) -> calcTotal.run());
        calcTotal.run();

        TextField tfDate = new TextField(LocalDate.now().toString());

        grid.add(new Label("Transaction ID:"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(new Label("Select Customer:"), 0, 1); grid.add(cbCustomer, 1, 1);
        grid.add(new Label("Select Product:"), 0, 2); grid.add(cbProduct, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3); grid.add(spQty, 1, 3);
        grid.add(new Label("Total Calculation:"), 0, 4); grid.add(lblTotal, 1, 4);
        grid.add(new Label("Transaction Date:"), 0, 5); grid.add(tfDate, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            try {
                String id = tfId.getText().trim();
                Customer cust = cbCustomer.getValue();
                Product prod = cbProduct.getValue();
                int qty = spQty.getValue();
                String date = tfDate.getText().trim();

                if (cust == null || prod == null) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Selection Error", "Missing Entity", "Please select both a customer and a product.");
                    return;
                }

                double total = prod.getPrice() * qty;
                Transaction t = new Transaction(id, cust.getCustomerId(), prod.getProductId(), qty, total, date);

                DataValidator.ValidationResult val = service.saveTransaction(t);
                if (!val.isValid()) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Transaction Data", val.getErrorMessage());
                }
            } catch (Exception ex) {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to record transaction", ex.getMessage());
            }
        }
    }

    public void refresh() {
        masterData.setAll(service.getAllTransactions());
        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        List<Transaction> filtered = masterData.stream().filter(t -> {
            if (q.isEmpty()) return true;
            return t.getTransactionId().toLowerCase().contains(q)
                    || t.getTransactionDate().contains(q)
                    || String.valueOf(t.getCustomerId()).contains(q)
                    || String.valueOf(t.getProductId()).contains(q);
        }).collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }
}
