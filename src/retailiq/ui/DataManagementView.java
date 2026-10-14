package retailiq.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retailiq.service.RetailIQService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataManagementView extends ScrollPane {

    private final RetailIQService service;

    private final VBox rootContent = new VBox(20);
    private final VBox statusCardsBox = new VBox(12);

    public DataManagementView(RetailIQService service) {
        this.service = service;

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
        Label title = new Label("Persistent Storage & Data Lifecycle Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label subtitle = new Label("Manage CSV repository files, create automated backups, and verify database integrity");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);
        rootContent.getChildren().add(titleBox);
    }

    private void buildContent() {
        // Quick Action Buttons
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button btnReload = new Button("🔄 Reload from CSV Files");
        btnReload.getStyleClass().add("btn-secondary");
        btnReload.setOnAction(e -> {
            boolean confirm = UIHelper.showConfirm("Reload CSV Data", "Reload all files from disk?",
                    "This will reread products.csv, customers.csv, reviews.csv, transactions.csv, and warehouses.csv from disk.");
            if (confirm) {
                service.reloadAll();
                UIHelper.showAlert(Alert.AlertType.INFORMATION, "Data Reloaded", "Success", "All repositories successfully reloaded from CSV files.");
            }
        });

        Button btnSave = new Button("💾 Force Save to CSV Files");
        btnSave.getStyleClass().add("btn-primary");
        btnSave.setOnAction(e -> {
            service.persistAll();
            UIHelper.showAlert(Alert.AlertType.INFORMATION, "Data Saved", "Success", "All in-memory tables persisted safely to disk.");
        });

        Button btnBackup = new Button("📦 Create Timestamped Backup");
        btnBackup.getStyleClass().add("btn-success");
        btnBackup.setOnAction(e -> createBackup());

        actionsRow.getChildren().addAll(btnReload, btnSave, btnBackup);

        // Status Container
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        Label statusHeader = new Label("Persistent CSV Repositories Status");
        statusHeader.getStyleClass().add("card-header");
        tableCard.getChildren().addAll(statusHeader, statusCardsBox);

        rootContent.getChildren().addAll(actionsRow, tableCard);
    }

    private void createBackup() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backupDir = new File("data" + File.separator + "backups" + File.separator + "backup_" + timestamp);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String[] files = {"products.csv", "customers.csv", "reviews.csv", "transactions.csv", "warehouses.csv"};
        int copied = 0;
        for (String fName : files) {
            File src = new File("data" + File.separator + fName);
            if (src.exists()) {
                File dest = new File(backupDir, fName);
                try {
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        UIHelper.showAlert(Alert.AlertType.INFORMATION, "Backup Created", "Archive Created Successfully",
                "Created persistent snapshot of " + copied + " CSV files in:\n" + backupDir.getAbsolutePath());
    }

    public void refresh() {
        statusCardsBox.getChildren().clear();

        addFileRow("Products Database", "data/products.csv", service.getAllProducts().size(), "Active Catalog Items");
        addFileRow("Customers Database", "data/customers.csv", service.getAllCustomers().size(), "Registered Accounts");
        addFileRow("Reviews Database", "data/reviews.csv", service.getAllReviews().size(), "Customer Feedback Entries");
        addFileRow("Transactions Database", "data/transactions.csv", service.getAllTransactions().size(), "Completed Orders");
        addFileRow("Warehouses Database", "data/warehouses.csv", service.getAllWarehouses().size(), "Distribution Hubs");
    }

    private void addFileRow(String name, String relPath, int rowCount, String desc) {
        HBox row = new HBox(16);
        row.setStyle("-fx-background-color: #0f172a; -fx-padding: 12px 16px; -fx-background-radius: 8px;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        titleBox.setPrefWidth(220);
        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc; -fx-font-size: 13.5px;");
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11.5px;");
        titleBox.getChildren().addAll(lblName, lblDesc);

        File f = new File(relPath);
        Label lblPath = new Label(f.getAbsolutePath());
        lblPath.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11.5px;");
        HBox.setHgrow(lblPath, Priority.ALWAYS);

        Label lblCount = new Label(rowCount + " records");
        lblCount.getStyleClass().add("badge-blue");

        Label lblSize = new Label(f.exists() ? (f.length() + " bytes") : "0 bytes");
        lblSize.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        Label lblStatus = new Label(f.exists() ? "● ONLINE" : "○ MISSING");
        lblStatus.setStyle(f.exists() ? "-fx-text-fill: #34d399; -fx-font-weight: bold;" : "-fx-text-fill: #f87171; -fx-font-weight: bold;");

        row.getChildren().addAll(titleBox, lblPath, lblCount, lblSize, lblStatus);
        statusCardsBox.getChildren().add(row);
    }
}
