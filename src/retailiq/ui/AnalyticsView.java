package retailiq.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import retailiq.service.AnalyticsEngine;
import retailiq.service.RetailIQService;

import java.util.Map;

public class AnalyticsView extends ScrollPane {

    private final RetailIQService service;
    private final AnalyticsEngine analytics;

    private final VBox rootContent = new VBox(20);
    private final HBox kpiContainer = new HBox(16);
    private final GridPane chartsGrid = new GridPane();

    public AnalyticsView(RetailIQService service) {
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
        VBox titleBox = new VBox(2);
        Label title = new Label("Executive Analytics & Financial Intelligence");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Multi-dimensional business analytics derived mathematically from persistent store transactions");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        rootContent.getChildren().add(titleBox);
    }

    private void buildContent() {
        chartsGrid.setHgap(16);
        chartsGrid.setVgap(16);

        rootContent.getChildren().addAll(kpiContainer, chartsGrid);
    }

    public void refresh() {
        // KPI row
        kpiContainer.getChildren().clear();
        kpiContainer.getChildren().addAll(
                UIHelper.createKpiCard("GROSS REVENUE", UIHelper.formatCurrency(analytics.getTotalRevenue()), "All Transactions", "emerald"),
                UIHelper.createKpiCard("AVG ORDER VALUE", UIHelper.formatCurrency(analytics.getAverageOrderValue()), "Per Basket Spend", "blue"),
                UIHelper.createKpiCard("TOTAL INVENTORY", analytics.getTotalInventoryUnits() + " units", "Across All Categories", "amber"),
                UIHelper.createKpiCard("CUSTOMER RATING", String.format("%.2f / 5.0 ★", analytics.getAverageReviewRating()), "Based on " + service.getAllReviews().size() + " Reviews", "emerald")
        );

        // Charts Grid
        chartsGrid.getChildren().clear();

        // 1. Revenue by Date (Line)
        VBox card1 = createChartCard("Revenue Timeline (Sales Trend)");
        CategoryAxis x1 = new CategoryAxis(); x1.setLabel("Date");
        NumberAxis y1 = new NumberAxis(); y1.setLabel("Sales (Rs.)");
        LineChart<String, Number> chart1 = new LineChart<>(x1, y1);
        chart1.setLegendVisible(false);
        chart1.setPrefHeight(260);
        XYChart.Series<String, Number> s1 = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : analytics.getSalesByDate().entrySet()) {
            s1.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart1.getData().add(s1);
        card1.getChildren().add(chart1);
        chartsGrid.add(card1, 0, 0);

        // 2. Revenue by Category (Bar)
        VBox card2 = createChartCard("Category Revenue Distribution");
        CategoryAxis x2 = new CategoryAxis(); x2.setLabel("Category");
        NumberAxis y2 = new NumberAxis(); y2.setLabel("Revenue (Rs.)");
        BarChart<String, Number> chart2 = new BarChart<>(x2, y2);
        chart2.setLegendVisible(false);
        chart2.setPrefHeight(260);
        XYChart.Series<String, Number> s2 = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : analytics.getRevenueByCategory().entrySet()) {
            s2.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart2.getData().add(s2);
        card2.getChildren().add(chart2);
        chartsGrid.add(card2, 1, 0);

        // 3. Rating Distribution (Bar)
        VBox card3 = createChartCard("Customer Satisfaction Breakdown (1 to 5 Stars)");
        CategoryAxis x3 = new CategoryAxis(); x3.setLabel("Star Rating");
        NumberAxis y3 = new NumberAxis(); y3.setLabel("Number of Reviews");
        BarChart<String, Number> chart3 = new BarChart<>(x3, y3);
        chart3.setLegendVisible(false);
        chart3.setPrefHeight(260);
        XYChart.Series<String, Number> s3 = new XYChart.Series<>();
        for (Map.Entry<Integer, Integer> e : analytics.getRatingDistribution().entrySet()) {
            s3.getData().add(new XYChart.Data<>(e.getKey() + " Stars", e.getValue()));
        }
        chart3.getData().add(s3);
        card3.getChildren().add(chart3);
        chartsGrid.add(card3, 0, 1);

        // 4. Products by Category (Bar)
        VBox card4 = createChartCard("Catalog SKU Count by Category");
        CategoryAxis x4 = new CategoryAxis(); x4.setLabel("Category");
        NumberAxis y4 = new NumberAxis(); y4.setLabel("Total SKUs");
        BarChart<String, Number> chart4 = new BarChart<>(x4, y4);
        chart4.setLegendVisible(false);
        chart4.setPrefHeight(260);
        XYChart.Series<String, Number> s4 = new XYChart.Series<>();
        for (Map.Entry<String, Integer> e : analytics.getProductCountByCategory().entrySet()) {
            s4.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart4.getData().add(s4);
        card4.getChildren().add(chart4);
        chartsGrid.add(card4, 1, 1);

        // Set column constraints
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        chartsGrid.getColumnConstraints().setAll(col1, col2);
    }

    private VBox createChartCard(String title) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label lbl = new Label(title);
        lbl.getStyleClass().add("card-header");
        card.getChildren().add(lbl);
        return card;
    }
}
