package retailiq.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class UIHelper {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static String formatCurrency(double amount) {
        String formatted = CURRENCY_FORMAT.format(amount);
        // Replace rupee symbol if needed for clean display
        return formatted.replace("₹", "Rs. ");
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    public static VBox createKpiCard(String title, String value, String subtext, String badgeType) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("kpi-value");

        Label lblSubtext = new Label(subtext);
        if ("emerald".equals(badgeType)) {
            lblSubtext.getStyleClass().add("badge-emerald");
        } else if ("amber".equals(badgeType)) {
            lblSubtext.getStyleClass().add("badge-amber");
        } else if ("rose".equals(badgeType)) {
            lblSubtext.getStyleClass().add("badge-rose");
        } else {
            lblSubtext.getStyleClass().add("badge-blue");
        }

        card.getChildren().addAll(lblTitle, lblValue, lblSubtext);
        return card;
    }

    public static HBox createInsightCard(String insightText) {
        HBox card = new HBox(12);
        card.getStyleClass().add("insight-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("💡");
        icon.setStyle("-fx-font-size: 16px;");

        Label text = new Label(insightText);
        text.getStyleClass().add("insight-text");
        text.setWrapText(true);
        HBox.setHgrow(text, Priority.ALWAYS);

        card.getChildren().addAll(icon, text);
        return card;
    }

    public static Label createHeader(String title, String subtitle) {
        Label header = new Label(title);
        header.getStyleClass().add("card-header");
        return header;
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(UIHelper.class.getResource("/retailiq/ui/Theme.css") != null ?
                UIHelper.class.getResource("/retailiq/ui/Theme.css").toExternalForm() : "");
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
