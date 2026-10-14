package retailiq.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.service.RetailIQService;

public class DataExplorerView extends VBox {

    private final RetailIQService service;

    private final ToggleGroup entityGroup = new ToggleGroup();
    private final StackPane viewContainer = new StackPane();

    private final ProductsView productsView;
    private final CustomersView customersView;
    private final TransactionsView transactionsView;
    private final ReviewsView reviewsView;
    private final WarehousesView warehousesView;

    public DataExplorerView(RetailIQService service) {
        this.service = service;

        this.productsView = new ProductsView(service);
        this.customersView = new CustomersView(service);
        this.transactionsView = new TransactionsView(service);
        this.reviewsView = new ReviewsView(service);
        this.warehousesView = new WarehousesView(service);

        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("main-container");

        buildHeader();
        buildContent();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(2);
        Label title = new Label("Unified Retail Data Explorer");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Inspect, filter, search, and manage all persistent retail entities from a single unified workspace");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);

        HBox toggleBar = new HBox(10);
        toggleBar.setAlignment(Pos.CENTER_LEFT);

        ToggleButton tbProd = createToggleButton("Products (" + service.getAllProducts().size() + ")", productsView);
        ToggleButton tbCust = createToggleButton("Customers (" + service.getAllCustomers().size() + ")", customersView);
        ToggleButton tbTxn = createToggleButton("Transactions (" + service.getAllTransactions().size() + ")", transactionsView);
        ToggleButton tbRev = createToggleButton("Reviews (" + service.getAllReviews().size() + ")", reviewsView);
        ToggleButton tbWh = createToggleButton("Warehouses (" + service.getAllWarehouses().size() + ")", warehousesView);

        tbProd.setSelected(true);
        toggleBar.getChildren().addAll(tbProd, tbCust, tbTxn, tbRev, tbWh);

        service.addChangeListener(() -> {
            tbProd.setText("Products (" + service.getAllProducts().size() + ")");
            tbCust.setText("Customers (" + service.getAllCustomers().size() + ")");
            tbTxn.setText("Transactions (" + service.getAllTransactions().size() + ")");
            tbRev.setText("Reviews (" + service.getAllReviews().size() + ")");
            tbWh.setText("Warehouses (" + service.getAllWarehouses().size() + ")");
        });

        getChildren().addAll(titleBox, toggleBar);
    }

    private ToggleButton createToggleButton(String text, javafx.scene.Node targetView) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(entityGroup);
        tb.getStyleClass().add("btn-secondary");

        tb.selectedProperty().addListener((obs, oldV, isSel) -> {
            if (isSel) {
                tb.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
                viewContainer.getChildren().setAll(targetView);
            } else {
                tb.setStyle(null);
            }
        });

        return tb;
    }

    private void buildContent() {
        VBox.setVgrow(viewContainer, Priority.ALWAYS);
        viewContainer.getChildren().setAll(productsView);
        getChildren().add(viewContainer);
    }
}
