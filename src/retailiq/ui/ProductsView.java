package retailiq.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.Product;
import retailiq.Review;
import retailiq.Transaction;
import retailiq.csv.DataValidator;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductsView extends VBox {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final TableView<Product> table = new TableView<>();
    private final ObservableList<Product> masterData = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryCombo = new ComboBox<>();

    // Detail Panel
    private final VBox detailPane = new VBox(12);

    public ProductsView(RetailIQService service) {
        this.service = service;
        this.analytics = new AnalyticsEngine(service);

        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("main-container");

        buildHeaderAndControls();
        buildContent();

        refresh();
        service.addChangeListener(this::refresh);
    }

    private void buildHeaderAndControls() {
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Product Catalog & Intelligence");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Manage retail inventory, pricing, catalog metadata, and live item performance");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add New Product");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showProductDialog(null));

        topRow.getChildren().addAll(titleBox, btnAdd);

        // Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search by name, code, or description...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        categoryCombo.setPromptText("All Categories");
        categoryCombo.setPrefWidth(160);
        categoryCombo.setOnAction(e -> applyFilter());

        Button btnReset = new Button("Reset Filters");
        btnReset.getStyleClass().add("btn-secondary");
        btnReset.setOnAction(e -> {
            searchField.clear();
            categoryCombo.getSelectionModel().selectFirst();
            applyFilter();
        });

        filterBar.getChildren().addAll(searchField, categoryCombo, btnReset);

        getChildren().addAll(topRow, filterBar);
    }

    private void buildContent() {
        HBox mainSplit = new HBox(16);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        // Left Table
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("card");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        buildTableColumns();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableBox.getChildren().add(table);

        // Right Detail Card
        detailPane.getStyleClass().add("card");
        detailPane.setPrefWidth(360);
        detailPane.setMinWidth(320);
        renderEmptyDetail();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                renderProductDetail(newVal);
            } else {
                renderEmptyDetail();
            }
        });

        mainSplit.getChildren().addAll(tableBox, detailPane);
        getChildren().add(mainSplit);
    }

    private void buildTableColumns() {
        TableColumn<Product, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getProductId()));
        colId.setPrefWidth(60);

        TableColumn<Product, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProductCode()));
        colCode.setPrefWidth(95);

        TableColumn<Product, String> colName = new TableColumn<>("Product Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(190);

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colCat.setPrefWidth(110);

        TableColumn<Product, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.formatCurrency(d.getValue().getPrice())));
        colPrice.setPrefWidth(100);

        TableColumn<Product, Product> colStock = new TableColumn<>("Stock Status");
        colStock.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colStock.setCellFactory(col -> new TableCell<Product, Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(item.getStock() + " units");
                    if (item.getStock() <= 10) {
                        lbl.getStyleClass().add("badge-rose");
                    } else if (item.getStock() <= 20) {
                        lbl.getStyleClass().add("badge-amber");
                    } else {
                        lbl.getStyleClass().add("badge-emerald");
                    }
                    setGraphic(lbl);
                }
            }
        });
        colStock.setPrefWidth(110);

        TableColumn<Product, String> colRating = new TableColumn<>("Rating");
        colRating.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f ★", d.getValue().getRating())));
        colRating.setPrefWidth(70);

        TableColumn<Product, Product> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<Product, Product>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDel = new Button("Delete");
            private final HBox pane = new HBox(6, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Product p = getItem();
                    if (p != null) showProductDialog(p);
                });
                btnDel.setOnAction(e -> {
                    Product p = getItem();
                    if (p != null) handleDeleteProduct(p);
                });
            }

            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        colActions.setPrefWidth(140);

        table.getColumns().addAll(colId, colCode, colName, colCat, colPrice, colStock, colRating, colActions);
    }

    private void renderEmptyDetail() {
        detailPane.getChildren().clear();
        Label title = new Label("Product Intelligence");
        title.getStyleClass().add("card-header");
        Label prompt = new Label("Select any product from the table to inspect full retail analytics, sales performance, inventory valuation, and customer reviews.");
        prompt.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        prompt.setWrapText(true);
        detailPane.getChildren().addAll(title, prompt);
    }

    private void renderProductDetail(Product p) {
        detailPane.getChildren().clear();

        Label title = new Label("Product Intelligence");
        title.getStyleClass().add("card-header");

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        name.setWrapText(true);

        Label meta = new Label(p.getCategory() + " • Code: " + p.getProductCode() + " • ID: #" + p.getProductId());
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        Label desc = new Label(p.getDescription());
        desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        desc.setWrapText(true);

        Separator sep1 = new Separator();

        // Stats grid
        GridPane stats = new GridPane();
        stats.setHgap(10);
        stats.setVgap(8);

        double valuation = p.getPrice() * p.getStock();
        List<Transaction> txns = analytics.getProductTransactions(p.getProductId());
        int unitsSold = txns.stream().mapToInt(Transaction::getQuantity).sum();
        double rev = txns.stream().mapToDouble(Transaction::getTotalAmount).sum();

        stats.add(new Label("Unit Price:"), 0, 0);
        stats.add(new Label(UIHelper.formatCurrency(p.getPrice())), 1, 0);

        stats.add(new Label("Current Stock:"), 0, 1);
        stats.add(new Label(p.getStock() + " units"), 1, 1);

        stats.add(new Label("Inventory Valuation:"), 0, 2);
        stats.add(new Label(UIHelper.formatCurrency(valuation)), 1, 2);

        stats.add(new Label("Historical Sales:"), 0, 3);
        stats.add(new Label(unitsSold + " units sold (Rs. " + String.format("%,.0f", rev) + ")"), 1, 3);

        stats.add(new Label("Catalog Rating:"), 0, 4);
        stats.add(new Label(String.format("%.1f / 5.0 ★", p.getRating())), 1, 4);

        for (int i = 0; i < stats.getChildren().size(); i++) {
            if (GridPane.getColumnIndex(stats.getChildren().get(i)) == 0) {
                stats.getChildren().get(i).setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            } else {
                stats.getChildren().get(i).setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 12px;");
            }
        }

        Separator sep2 = new Separator();

        // Customer Reviews
        Label revHeader = new Label("Associated Reviews (" + analytics.getProductReviews(p.getProductId()).size() + ")");
        revHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc; -fx-font-size: 13px;");

        VBox revList = new VBox(8);
        List<Review> pReviews = analytics.getProductReviews(p.getProductId());
        if (pReviews.isEmpty()) {
            Label noRev = new Label("No customer reviews recorded yet.");
            noRev.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            revList.getChildren().add(noRev);
        } else {
            for (Review r : pReviews) {
                VBox revCard = new VBox(2);
                revCard.setStyle("-fx-background-color: #0f172a; -fx-padding: 8px; -fx-background-radius: 6px;");
                Label stars = new Label("★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating()));
                stars.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
                Label comment = new Label(r.getReviewText());
                comment.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11.5px;");
                comment.setWrapText(true);
                revCard.getChildren().addAll(stars, comment);
                revList.getChildren().add(revCard);
            }
        }

        ScrollPane scrollReviews = new ScrollPane(revList);
        scrollReviews.setFitToWidth(true);
        scrollReviews.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollReviews.setPrefHeight(150);

        detailPane.getChildren().addAll(title, name, meta, desc, sep1, stats, sep2, revHeader, scrollReviews);
    }

    private void handleDeleteProduct(Product p) {
        boolean confirm = UIHelper.showConfirm("Delete Product", "Delete " + p.getName() + "?",
                "Are you sure you want to delete Product #" + p.getProductId() + "? This action persists directly to products.csv.");
        if (confirm) {
            service.deleteProduct(p.getProductId());
        }
    }

    private void showProductDialog(Product existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Product" : "Edit Product #" + existing.getProductId());
        dialog.setHeaderText(existing == null ? "Register a new product in RetailIQ catalog" : "Modify product information");

        ButtonType btnSaveType = new ButtonType("Save to CSV", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField tfId = new TextField(existing != null ? String.valueOf(existing.getProductId()) : String.valueOf(getNextId()));
        if (existing != null) tfId.setDisable(true);
        TextField tfName = new TextField(existing != null ? existing.getName() : "");
        TextField tfCat = new TextField(existing != null ? existing.getCategory() : "");
        TextField tfCode = new TextField(existing != null ? existing.getProductCode() : "");
        TextField tfPrice = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "0.0");
        TextField tfStock = new TextField(existing != null ? String.valueOf(existing.getStock()) : "0");
        TextField tfRating = new TextField(existing != null ? String.valueOf(existing.getRating()) : "5.0");
        TextArea taDesc = new TextArea(existing != null ? existing.getDescription() : "");
        taDesc.setPrefRowCount(3);

        grid.add(new Label("Product ID:"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(new Label("Product Name:"), 0, 1); grid.add(tfName, 1, 1);
        grid.add(new Label("Category:"), 0, 2); grid.add(tfCat, 1, 2);
        grid.add(new Label("Product Code:"), 0, 3); grid.add(tfCode, 1, 3);
        grid.add(new Label("Price (Rs.):"), 0, 4); grid.add(tfPrice, 1, 4);
        grid.add(new Label("Stock:"), 0, 5); grid.add(tfStock, 1, 5);
        grid.add(new Label("Rating (0.0-5.0):"), 0, 6); grid.add(tfRating, 1, 6);
        grid.add(new Label("Description:"), 0, 7); grid.add(taDesc, 1, 7);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                String name = tfName.getText().trim();
                String cat = tfCat.getText().trim();
                String code = tfCode.getText().trim();
                double price = Double.parseDouble(tfPrice.getText().trim());
                int stock = Integer.parseInt(tfStock.getText().trim());
                double rating = Double.parseDouble(tfRating.getText().trim());
                String desc = taDesc.getText().trim();

                Product p = new Product(id, name, cat, desc, price, stock, rating, code);
                DataValidator.ValidationResult val = service.saveProduct(p);
                if (!val.isValid()) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Product Data", val.getErrorMessage());
                }
            } catch (Exception ex) {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Input Error", "Failed to parse inputs", ex.getMessage());
            }
        }
    }

    private int getNextId() {
        return service.getAllProducts().stream().mapToInt(Product::getProductId).max().orElse(100) + 1;
    }

    public void refresh() {
        masterData.setAll(service.getAllProducts());

        // Update category combo
        String currentCat = categoryCombo.getValue();
        List<String> categories = service.getAllProducts().stream()
                .map(Product::getCategory).distinct().sorted().collect(Collectors.toList());
        categories.add(0, "All Categories");
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        if (currentCat != null && categories.contains(currentCat)) {
            categoryCombo.setValue(currentCat);
        } else {
            categoryCombo.getSelectionModel().selectFirst();
        }

        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String cat = categoryCombo.getValue();

        List<Product> filtered = masterData.stream().filter(p -> {
            boolean matchesCat = cat == null || cat.equals("All Categories") || p.getCategory().equalsIgnoreCase(cat);
            boolean matchesQuery = q.isEmpty()
                    || p.getName().toLowerCase().contains(q)
                    || p.getProductCode().toLowerCase().contains(q)
                    || p.getDescription().toLowerCase().contains(q)
                    || String.valueOf(p.getProductId()).contains(q);
            return matchesCat && matchesQuery;
        }).collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }
}
