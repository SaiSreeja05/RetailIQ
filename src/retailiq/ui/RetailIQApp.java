package retailiq.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import retailiq.service.RetailIQService;

public class RetailIQApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        RetailIQService service = RetailIQService.getInstance();
        MainView mainView = new MainView(service);

        Scene scene = new Scene(mainView, 1280, 820);
        String cssPath = getClass().getResource("/retailiq/ui/Theme.css") != null ?
                getClass().getResource("/retailiq/ui/Theme.css").toExternalForm() : null;

        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            // Fallback to local file or direct styling if running from bin
            try {
                java.io.File cssFile = new java.io.File("src/retailiq/ui/Theme.css");
                if (cssFile.exists()) {
                    scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
                }
            } catch (Exception ignored) {}
        }

        primaryStage.setTitle("RetailIQ — Retail Intelligence & Analytics Platform");
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
