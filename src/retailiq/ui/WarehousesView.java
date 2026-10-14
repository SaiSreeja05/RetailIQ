package retailiq.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.Product;
import retailiq.Warehouse;
import retailiq.csv.DataValidator;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.List;
import java.util.Optional;

public class WarehousesView extends ScrollPane {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final VBox rootContent = new VBox(20);
    private final FlowPane cardsContainer = new FlowPane(16, 16);
    private final TableView<Warehouse> table = new TableView<>();
    private final ObservableList<Warehouse> masterData = FXCollections.observableArrayList();

    public WarehousesView(RetailIQService service) {
        this.service = service;
        this.analytics = new AnalyticsEngine(service);

        setFitToWidth(true);
        getStyleClass().add("main-container");
        setStyle("-fx-background: #0b0f19; -fx-background-color: transparent;");

        rootContent.setPadding(new Insets(24));
        setContent(rootContent);

        buildHeader();
        buildContent();

        refresh();
        service.addChangeListener(this::refresh);
    }

    private void buildHeader() {
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Warehouse Logistics & Storage Network");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Monitor regional distribution centers, storage utilization, and stock allocation");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add New Warehouse");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showWarehouseDialog(null));

        topRow.getChildren().addAll(titleBox, btnAdd);
        rootContent.getChildren().add(topRow);
    }

    private void buildContent() {
        // Warehouse Capacity Cards
        VBox cardsSection = new VBox(10);
        Label lblSection = new Label("Regional Distribution Centers Overview");
        lblSection.getStyleClass().add("card-header");
        cardsSection.getChildren().addAll(lblSection, cardsContainer);

        // Warehouses Table
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("card");

        Label tableTitle = new Label("Warehouse Directory");
        tableTitle.getStyleClass().add("card-header");

        buildTableColumns();
        table.setPrefHeight(220);
        tableBox.getChildren().addAll(tableTitle, table);

        // Low stock reorder priority
        VBox reorderBox = new VBox(10);
        reorderBox.getStyleClass().add("card");

        Label reorderTitle = new Label("Warehouse Reorder & Restocking Priority (Stock <= 20)");
        reorderTitle.getStyleClass().add("card-header");

        TableView<Product> reorderTable = new TableView<>();
        reorderTable.setPrefHeight(180);

        TableColumn<Product, String> colPName = new TableColumn<>("Product");
        colPName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPName.setPrefWidth(200);

        TableColumn<Product, String> colPCat = new TableColumn<>("Category");
        colPCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colPCat.setPrefWidth(120);

        TableColumn<Product, Number> colPStock = new TableColumn<>("Units Remaining");
        colPStock.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()));
        colPStock.setPrefWidth(120);

        TableColumn<Product, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStock() <= 10 ? "CRITICAL REORDER" : "WARNING"));
        colStatus.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add(item.contains("CRITICAL") ? "badge-rose" : "badge-amber");
                    setGraphic(lbl);
                }
            }
        });
        colStatus.setPrefWidth(160);

        reorderTable.getColumns().addAll(colPName, colPCat, colPStock, colStatus);
        reorderTable.getItems().setAll(analytics.getLowStockProducts(20));
        reorderBox.getChildren().addAll(reorderTitle, reorderTable);

        rootContent.getChildren().addAll(cardsSection, tableBox, reorderBox);
    }

    private void buildTableColumns() {
        TableColumn<Warehouse, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getWarehouseId()));
        colId.setPrefWidth(70);

        TableColumn<Warehouse, String> colName = new TableColumn<>("Warehouse Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getWarehouseName()));
        colName.setPrefWidth(260);

        TableColumn<Warehouse, String> colLoc = new TableColumn<>("Location");
        colLoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));
        colLoc.setPrefWidth(160);

        TableColumn<Warehouse, String> colCap = new TableColumn<>("Storage Capacity");
        colCap.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,d units", d.getValue().getCapacity())));
        colCap.setPrefWidth(140);

        TableColumn<Warehouse, Warehouse> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<Warehouse, Warehouse>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDel = new Button("Delete");
            private final HBox pane = new HBox(6, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Warehouse w = getItem();
                    if (w != null) showWarehouseDialog(w);
                });
                btnDel.setOnAction(e -> {
                    Warehouse w = getItem();
                    if (w != null) handleDeleteWarehouse(w);
                });
            }

            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        colActions.setPrefWidth(140);

        table.getColumns().addAll(colId, colName, colLoc, colCap, colActions);
    }

    private void handleDeleteWarehouse(Warehouse w) {
        boolean confirm = UIHelper.showConfirm("Delete Warehouse", "Delete " + w.getWarehouseName() + "?",
                "Are you sure you want to remove Warehouse #" + w.getWarehouseId() + "? This action persists directly to warehouses.csv.");
        if (confirm) {
            service.deleteWarehouse(w.getWarehouseId());
        }
    }

    private void showWarehouseDialog(Warehouse existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Warehouse" : "Edit Warehouse #" + existing.getWarehouseId());
        dialog.setHeaderText(existing == null ? "Register a new storage or fulfillment hub" : "Modify warehouse attributes");

        ButtonType btnSaveType = new ButtonType("Save to CSV", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField tfId = new TextField(existing != null ? String.valueOf(existing.getWarehouseId()) : String.valueOf(getNextId()));
        if (existing != null) tfId.setDisable(true);
        TextField tfName = new TextField(existing != null ? existing.getWarehouseName() : "");
        TextField tfLoc = new TextField(existing != null ? existing.getLocation() : "");
        TextField tfCap = new TextField(existing != null ? String.valueOf(existing.getCapacity()) : "5000");

        grid.add(new Label("Warehouse ID:"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(new Label("Warehouse Name:"), 0, 1); grid.add(tfName, 1, 1);
        grid.add(new Label("Location:"), 0, 2); grid.add(tfLoc, 1, 2);
        grid.add(new Label("Capacity (Units):"), 0, 3); grid.add(tfCap, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String name = tfName.getText().trim();
                String loc = tfLoc.getText().trim();
                int cap = Integer.parseInt(tfCap.getText().trim());

                Warehouse w = new Warehouse(id, name, loc, cap);
                DataValidator.ValidationResult val = service.saveWarehouse(w);
                if (!val.isValid()) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Warehouse Data", val.getErrorMessage());
                }
            } catch (Exception ex) {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Input Error", "Failed to parse inputs", ex.getMessage());
            }
        }
    }

    private int getNextId() {
        return service.getAllWarehouses().stream().mapToInt(Warehouse::getWarehouseId).max().orElse(400) + 1;
    }

    public void refresh() {
        masterData.setAll(service.getAllWarehouses());
        table.setItems(masterData);

        cardsContainer.getChildren().clear();
        for (Warehouse w : masterData) {
            VBox card = new VBox(8);
            card.getStyleClass().add("card");
            card.setPrefWidth(220);

            Label title = new Label(w.getWarehouseName());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc; -fx-font-size: 13.5px;");
            title.setWrapText(true);

            Label loc = new Label("📍 " + w.getLocation());
            loc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

            ProgressBar bar = new ProgressBar(0.4);
            bar.setPrefWidth(200);

            Label cap = new Label("Capacity: " + String.format("%,d units", w.getCapacity()));
            cap.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11.5px; -fx-font-weight: bold;");

            card.getChildren().addAll(title, loc, bar, cap);
            cardsContainer.getChildren().add(card);
        }
    }
}
