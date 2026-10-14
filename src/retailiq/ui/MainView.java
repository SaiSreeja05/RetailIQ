package retailiq.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.service.RetailIQService;
import retailiq.service.SmartSearchEngine;

import java.util.ArrayList;
import java.util.List;

public class MainView extends BorderPane {

    private final RetailIQService service;
    private final SmartSearchEngine searchEngine;

    private final VBox sidebar = new VBox(6);
    private final StackPane contentArea = new StackPane();
    private final List<Button> navButtons = new ArrayList<>();

    // Views
    private final DashboardView dashboardView;
    private final ProductsView productsView;
    private final CustomersView customersView;
    private final TransactionsView transactionsView;
    private final WarehousesView warehousesView;
    private final ReviewsView reviewsView;
    private final DataExplorerView dataExplorerView;
    private final AnalyticsView analyticsView;
    private final AlgorithmsLabView algorithmsLabView;
    private final DataManagementView dataManagementView;

    // Search popup
    private final ContextMenu searchPopup = new ContextMenu();

    public MainView(RetailIQService service) {
        this.service = service;
        this.searchEngine = new SmartSearchEngine(service);

        this.dashboardView = new DashboardView(service);
        this.productsView = new ProductsView(service);
        this.customersView = new CustomersView(service);
        this.transactionsView = new TransactionsView(service);
        this.warehousesView = new WarehousesView(service);
        this.reviewsView = new ReviewsView(service);
        this.dataExplorerView = new DataExplorerView(service);
        this.analyticsView = new AnalyticsView(service);
        this.algorithmsLabView = new AlgorithmsLabView(service);
        this.dataManagementView = new DataManagementView(service);

        buildTopBar();
        buildSidebar();

        setCenter(contentArea);
        switchView(dashboardView, navButtons.get(0));
    }

    private void buildTopBar() {
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 24, 12, 24));

        Label brand = new Label("RetailIQ");
        brand.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label badge = new Label("INTELLIGENCE PLATFORM");
        badge.getStyleClass().add("badge-blue");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Global Smart Search
        TextField searchInput = new TextField();
        searchInput.setPromptText("⚡ Global Smart Search (Products, Customers, Transactions, Warehouses)...");
        searchInput.setPrefWidth(460);
        searchInput.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.trim().isEmpty()) {
                searchPopup.hide();
            } else {
                List<SmartSearchEngine.SearchResult> results = searchEngine.search(newV.trim());
                showSearchResults(searchInput, results);
            }
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label storageStatus = new Label("● CSV Persistence Active");
        storageStatus.getStyleClass().add("badge-emerald");

        topBar.getChildren().addAll(brand, badge, spacer1, searchInput, spacer2, storageStatus);
        setTop(topBar);
    }

    private void showSearchResults(TextField target, List<SmartSearchEngine.SearchResult> results) {
        searchPopup.getItems().clear();
        if (results.isEmpty()) {
            MenuItem emptyItem = new MenuItem("No matching entities found.");
            emptyItem.setDisable(true);
            searchPopup.getItems().add(emptyItem);
        } else {
            for (int i = 0; i < Math.min(results.size(), 8); i++) {
                SmartSearchEngine.SearchResult r = results.get(i);
                MenuItem item = new MenuItem(String.format("[%s] %s — %s (%s)", r.getType(), r.getTitle(), r.getSubtitle(), r.getDetail()));
                item.setOnAction(e -> {
                    // Navigate to appropriate view
                    switch (r.getType()) {
                        case PRODUCT: switchView(productsView, navButtons.get(1)); break;
                        case CUSTOMER: switchView(customersView, navButtons.get(2)); break;
                        case TRANSACTION: switchView(transactionsView, navButtons.get(3)); break;
                        case WAREHOUSE: switchView(warehousesView, navButtons.get(4)); break;
                        case REVIEW: switchView(reviewsView, navButtons.get(5)); break;
                    }
                });
                searchPopup.getItems().add(item);
            }
        }
        if (!searchPopup.isShowing()) {
            searchPopup.show(target, javafx.geometry.Side.BOTTOM, 0, 4);
        }
    }

    private void buildSidebar() {
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(200);
        sidebar.setPadding(new Insets(16, 12, 16, 12));

        Label navTitle = new Label("NAVIGATION");
        navTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 0 0 6 8;");
        sidebar.getChildren().add(navTitle);

        Button btnDash = createNavButton("📊  Dashboard", dashboardView);
        Button btnProd = createNavButton("🏷️  Products", productsView);
        Button btnCust = createNavButton("👥  Customers", customersView);
        Button btnTxn = createNavButton("💳  Transactions", transactionsView);
        Button btnWh = createNavButton("🏢  Warehouses", warehousesView);
        Button btnRev = createNavButton("⭐  Reviews", reviewsView);
        Button btnExp = createNavButton("🔍  Data Explorer", dataExplorerView);
        Button btnAnalytics = createNavButton("📈  Analytics", analyticsView);
        Button btnAlgo = createNavButton("⚡  Algorithms Lab", algorithmsLabView);
        Button btnData = createNavButton("💾  Data Management", dataManagementView);

        sidebar.getChildren().addAll(
                btnDash, btnProd, btnCust, btnTxn, btnWh, btnRev,
                new Separator(),
                btnExp, btnAnalytics, btnAlgo, btnData
        );

        setLeft(sidebar);
    }

    private Button createNavButton(String title, javafx.scene.Node targetView) {
        Button btn = new Button(title);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> switchView(targetView, btn));
        navButtons.add(btn);
        return btn;
    }

    private void switchView(javafx.scene.Node view, Button activeBtn) {
        contentArea.getChildren().setAll(view);
        for (Button b : navButtons) {
            b.getStyleClass().remove("nav-button-active");
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }
}
