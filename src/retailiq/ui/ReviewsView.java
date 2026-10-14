package retailiq.ui;

import StringAlgorithms.KeywordMatcher;
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
import retailiq.csv.DataValidator;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.*;
import java.util.stream.Collectors;

public class ReviewsView extends VBox {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final TableView<Review> table = new TableView<>();
    private final ObservableList<Review> masterData = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final HBox ratingDistributionBar = new HBox(12);
    private final FlowPane keywordChips = new FlowPane(8, 8);

    public ReviewsView(RetailIQService service) {
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
        Label title = new Label("Customer Reviews & Sentiment Intelligence");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Analyze customer feedback, monitor product satisfaction scores, and extract sentiment trends");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add Customer Review");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showReviewDialog());

        topRow.getChildren().addAll(titleBox, btnAdd);
        getChildren().add(topRow);
    }

    private void buildContent() {
        // Sentiment & Ratings summary card
        VBox sentimentCard = new VBox(12);
        sentimentCard.getStyleClass().add("card");

        Label cardHeader = new Label("Customer Sentiment & Ratings Breakdown");
        cardHeader.getStyleClass().add("card-header");

        ratingDistributionBar.setAlignment(Pos.CENTER_LEFT);

        Label kwHeader = new Label("Keywords & Attributes Detected across Customer Feedback (Aho-Corasick NLP):");
        kwHeader.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");

        sentimentCard.getChildren().addAll(cardHeader, ratingDistributionBar, kwHeader, keywordChips);

        // Reviews Table Card
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Search reviews by comment text, product, or customer...");
        searchField.setPrefWidth(380);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter());
        filterBar.getChildren().add(searchField);

        buildTableColumns();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(filterBar, table);

        getChildren().addAll(sentimentCard, tableCard);
    }

    private void buildTableColumns() {
        TableColumn<Review, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getReviewId()));
        colId.setPrefWidth(60);

        TableColumn<Review, String> colProd = new TableColumn<>("Product");
        colProd.setCellValueFactory(d -> {
            int pId = d.getValue().getProductId();
            Optional<Product> opt = service.getProductRepo().findById(pId);
            return new SimpleStringProperty(opt.map(Product::getName).orElse("Product #" + pId));
        });
        colProd.setPrefWidth(180);

        TableColumn<Review, String> colCust = new TableColumn<>("Reviewer");
        colCust.setCellValueFactory(d -> {
            int cId = d.getValue().getCustomerId();
            Optional<Customer> opt = service.getCustomerRepo().findById(cId);
            return new SimpleStringProperty(opt.map(Customer::getName).orElse("Customer #" + cId));
        });
        colCust.setPrefWidth(140);

        TableColumn<Review, Review> colRating = new TableColumn<>("Rating");
        colRating.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colRating.setCellFactory(col -> new TableCell<Review, Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label("★".repeat(item.getRating()) + "☆".repeat(5 - item.getRating()));
                    lbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 13px;");
                    setGraphic(lbl);
                }
            }
        });
        colRating.setPrefWidth(110);

        TableColumn<Review, String> colText = new TableColumn<>("Customer Commentary");
        colText.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReviewText()));
        colText.setPrefWidth(360);

        TableColumn<Review, Review> colActions = new TableColumn<>("Actions");
        colActions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<Review, Review>() {
            private final Button btnDel = new Button("Delete");

            {
                btnDel.getStyleClass().add("btn-danger");
                btnDel.setOnAction(e -> {
                    Review r = getItem();
                    if (r != null) handleDeleteReview(r);
                });
            }

            @Override
            protected void updateItem(Review item, boolean empty) {
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
        colActions.setPrefWidth(90);

        table.getColumns().addAll(colId, colProd, colCust, colRating, colText, colActions);
    }

    private void handleDeleteReview(Review r) {
        boolean confirm = UIHelper.showConfirm("Delete Review", "Delete Review #" + r.getReviewId() + "?",
                "Are you sure you want to remove Review #" + r.getReviewId() + "? This action persists directly to reviews.csv.");
        if (confirm) {
            service.deleteReview(r.getReviewId());
        }
    }

    private void showReviewDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit Customer Review");
        dialog.setHeaderText("Add verified customer feedback and star rating to catalog");

        ButtonType btnSaveType = new ButtonType("Save to CSV", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField tfId = new TextField(String.valueOf(getNextId()));
        tfId.setDisable(true);

        ComboBox<Product> cbProduct = new ComboBox<>();
        cbProduct.setItems(FXCollections.observableArrayList(service.getAllProducts()));
        cbProduct.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (#" + item.getProductId() + ")");
            }
        });
        cbProduct.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (#" + item.getProductId() + ")");
            }
        });
        if (!cbProduct.getItems().isEmpty()) cbProduct.getSelectionModel().selectFirst();

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

        ComboBox<Integer> cbRating = new ComboBox<>();
        cbRating.setItems(FXCollections.observableArrayList(5, 4, 3, 2, 1));
        cbRating.setValue(5);

        TextArea taText = new TextArea();
        taText.setPromptText("Enter customer review commentary...");
        taText.setPrefRowCount(3);

        grid.add(new Label("Review ID:"), 0, 0); grid.add(tfId, 1, 0);
        grid.add(new Label("Product:"), 0, 1); grid.add(cbProduct, 1, 1);
        grid.add(new Label("Customer:"), 0, 2); grid.add(cbCustomer, 1, 2);
        grid.add(new Label("Star Rating:"), 0, 3); grid.add(cbRating, 1, 3);
        grid.add(new Label("Review Text:"), 0, 4); grid.add(taText, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            try {
                int id = Integer.parseInt(tfId.getText().trim());
                Product p = cbProduct.getValue();
                Customer c = cbCustomer.getValue();
                int rating = cbRating.getValue();
                String text = taText.getText().trim();

                if (p == null || c == null) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Selection Error", "Missing Entity", "Please select both a product and a customer.");
                    return;
                }

                Review review = new Review(id, p.getProductId(), c.getCustomerId(), text, rating);
                DataValidator.ValidationResult val = service.saveReview(review);
                if (!val.isValid()) {
                    UIHelper.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Review Data", val.getErrorMessage());
                }
            } catch (Exception ex) {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save review", ex.getMessage());
            }
        }
    }

    private int getNextId() {
        return service.getAllReviews().stream().mapToInt(Review::getReviewId).max().orElse(300) + 1;
    }

    public void refresh() {
        masterData.setAll(service.getAllReviews());

        // Update rating breakdown
        ratingDistributionBar.getChildren().clear();
        Map<Integer, Integer> dist = analytics.getRatingDistribution();
        int total = service.getAllReviews().size();
        for (int stars = 5; stars >= 1; stars--) {
            int count = dist.getOrDefault(stars, 0);
            double pct = total > 0 ? (double) count / total * 100.0 : 0.0;
            VBox box = UIHelper.createKpiCard(stars + " ★ Rating", count + " reviews", String.format("%.0f%% share", pct), "amber");
            box.setPrefWidth(140);
            ratingDistributionBar.getChildren().add(box);
        }

        // Run Aho-Corasick across all reviews
        keywordChips.getChildren().clear();
        List<String> targetKws = Arrays.asList("camera", "performance", "battery", "design", "noise cancellation",
                "sound", "comfortable", "lighting", "storage", "display", "lightweight", "smooth", "premium", "fast");
        KeywordMatcher matcher = new KeywordMatcher();
        for (String kw : targetKws) matcher.addKeyword(kw);
        matcher.build();

        Map<String, Integer> kwCounts = new HashMap<>();
        for (Review r : service.getAllReviews()) {
            ArrayList<String> found = matcher.search(r.getReviewText());
            for (String f : found) {
                kwCounts.put(f, kwCounts.getOrDefault(f, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : kwCounts.entrySet()) {
            Label chip = new Label(entry.getKey().toUpperCase() + " (" + entry.getValue() + ")");
            chip.getStyleClass().add("badge-blue");
            keywordChips.getChildren().add(chip);
        }

        applyFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        List<Review> filtered = masterData.stream().filter(r -> {
            if (q.isEmpty()) return true;
            return r.getReviewText().toLowerCase().contains(q)
                    || String.valueOf(r.getReviewId()).contains(q)
                    || String.valueOf(r.getProductId()).contains(q)
                    || String.valueOf(r.getCustomerId()).contains(q);
        }).collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }
}
