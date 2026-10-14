package retailiq.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.Product;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.List;
import java.util.Map;

public class DashboardView extends ScrollPane {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final VBox rootContent = new VBox(20);
    private final HBox kpiContainer = new HBox(16);
    private final VBox insightsContainer = new VBox(10);
    private final HBox chartsContainer = new HBox(16);
    private final HBox bottomTablesContainer = new HBox(16);

    public DashboardView(RetailIQService service) {
        this.service = service;
        this.analytics = new AnalyticsEngine(service);

        setFitToWidth(true);
        getStyleClass().add("main-container");
        setStyle("-fx-background: #0b0f19; -fx-background-color: transparent;");

        rootContent.setPadding(new Insets(24));
        rootContent.setFillWidth(true);
        setContent(rootContent);

        buildHeader();
        rootContent.getChildren().addAll(kpiContainer, insightsContainer, chartsContainer, bottomTablesContainer);

        refresh();
        service.addChangeListener(this::refresh);
    }

    private void buildHeader() {
        VBox headerBox = new VBox(4);
        Label title = new Label("RetailIQ — Turn Retail Data Into Decisions");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitle = new Label("Live Intelligence & Operational Health Dashboard • Connected to Persistent CSV Storage");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        headerBox.getChildren().addAll(title, subtitle);
        rootContent.getChildren().add(headerBox);
    }

    public void refresh() {
        // 1. KPI Cards
        kpiContainer.getChildren().clear();
        kpiContainer.getChildren().addAll(
                UIHelper.createKpiCard("TOTAL PRODUCTS", String.valueOf(analytics.getTotalProducts()), "Active Catalog Items", "blue"),
                UIHelper.createKpiCard("CUSTOMERS", String.valueOf(analytics.getTotalCustomers()), "Registered Accounts", "blue"),
                UIHelper.createKpiCard("TRANSACTIONS", String.valueOf(analytics.getTotalTransactions()), "Completed Orders", "blue"),
                UIHelper.createKpiCard("TOTAL REVENUE", UIHelper.formatCurrency(analytics.getTotalRevenue()), "Lifetime Sales", "emerald"),
                UIHelper.createKpiCard("AVG RATING", String.format("%.2f / 5.0", analytics.getAverageReviewRating()), "Customer Sentiment", "amber"),
                UIHelper.createKpiCard("INVENTORY UNITS", String.valueOf(analytics.getTotalInventoryUnits()), String.format("%.1f%% Network Cap", analytics.getWarehouseUtilizationRate()), "blue")
        );

        // 2. Dynamic Insights
        insightsContainer.getChildren().clear();
        Label insightsHeader = new Label("Real-time Retail Insights");
        insightsHeader.getStyleClass().add("card-header");
        insightsContainer.getChildren().add(insightsHeader);

        List<String> insights = analytics.generateDynamicInsights();
        for (String ins : insights) {
            insightsContainer.getChildren().add(UIHelper.createInsightCard(ins));
        }

        // 3. Charts
        chartsContainer.getChildren().clear();

        // Sales Trend Chart
        VBox salesBox = new VBox(10);
        salesBox.getStyleClass().add("card");
        HBox.setHgrow(salesBox, Priority.ALWAYS);

        Label salesTitle = new Label("Sales Revenue Trend (Timeline)");
        salesTitle.getStyleClass().add("card-header");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Transaction Date");
        yAxis.setLabel("Revenue (Rs.)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(null);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(280);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> salesByDate = analytics.getSalesByDate();
        for (Map.Entry<String, Double> entry : salesByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        lineChart.getData().add(series);
        salesBox.getChildren().addAll(salesTitle, lineChart);

        // Category Breakdown Chart
        VBox catBox = new VBox(10);
        catBox.getStyleClass().add("card");
        HBox.setHgrow(catBox, Priority.ALWAYS);

        Label catTitle = new Label("Revenue by Product Category");
        catTitle.getStyleClass().add("card-header");

        CategoryAxis catXAxis = new CategoryAxis();
        NumberAxis catYAxis = new NumberAxis();
        catXAxis.setLabel("Category");
        catYAxis.setLabel("Total Sales (Rs.)");

        BarChart<String, Number> barChart = new BarChart<>(catXAxis, catYAxis);
        barChart.setTitle(null);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(280);

        XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
        Map<String, Double> revByCat = analytics.getRevenueByCategory();
        for (Map.Entry<String, Double> entry : revByCat.entrySet()) {
            catSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(catSeries);
        catBox.getChildren().addAll(catTitle, barChart);

        chartsContainer.getChildren().addAll(salesBox, catBox);

        // 4. Bottom Tables: Top Sellers & Low Stock
        bottomTablesContainer.getChildren().clear();

        // Top Sellers Table
        VBox topSellersCard = new VBox(10);
        topSellersCard.getStyleClass().add("card");
        HBox.setHgrow(topSellersCard, Priority.ALWAYS);

        Label topSellersHeader = new Label("Top Purchased Products");
        topSellersHeader.getStyleClass().add("card-header");

        TableView<AnalyticsEngine.ProductSalesStats> topTable = new TableView<>();
        topTable.setPrefHeight(200);

        TableColumn<AnalyticsEngine.ProductSalesStats, String> colProd = new TableColumn<>("Product");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        colProd.setPrefWidth(180);

        TableColumn<AnalyticsEngine.ProductSalesStats, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getCategory()));
        colCat.setPrefWidth(100);

        TableColumn<AnalyticsEngine.ProductSalesStats, Number> colQty = new TableColumn<>("Qty Sold");
        colQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantitySold()));
        colQty.setPrefWidth(80);

        TableColumn<AnalyticsEngine.ProductSalesStats, String> colRev = new TableColumn<>("Revenue");
        colRev.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.formatCurrency(d.getValue().getTotalRevenue())));
        colRev.setPrefWidth(110);

        topTable.getColumns().addAll(colProd, colCat, colQty, colRev);
        topTable.getItems().setAll(analytics.getTopSellingProducts());
        topSellersCard.getChildren().addAll(topSellersHeader, topTable);

        // Low Stock Alert Table
        VBox lowStockCard = new VBox(10);
        lowStockCard.getStyleClass().add("card");
        HBox.setHgrow(lowStockCard, Priority.ALWAYS);

        Label lowStockHeader = new Label("Low Stock Inventory Alerts (<= 15 units)");
        lowStockHeader.getStyleClass().add("card-header");

        TableView<Product> lowTable = new TableView<>();
        lowTable.setPrefHeight(200);

        TableColumn<Product, String> colLProd = new TableColumn<>("Product");
        colLProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colLProd.setPrefWidth(180);

        TableColumn<Product, String> colLCode = new TableColumn<>("Code");
        colLCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProductCode()));
        colLCode.setPrefWidth(90);

        TableColumn<Product, Number> colLStock = new TableColumn<>("Stock Left");
        colLStock.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()));
        colLStock.setPrefWidth(80);

        TableColumn<Product, String> colLPrice = new TableColumn<>("Unit Price");
        colLPrice.setCellValueFactory(d -> new SimpleStringProperty(UIHelper.formatCurrency(d.getValue().getPrice())));
        colLPrice.setPrefWidth(110);

        lowTable.getColumns().addAll(colLProd, colLCode, colLStock, colLPrice);
        lowTable.getItems().setAll(analytics.getLowStockProducts(15));
        lowStockCard.getChildren().addAll(lowStockHeader, lowTable);

        bottomTablesContainer.getChildren().addAll(topSellersCard, lowStockCard);
    }
}
