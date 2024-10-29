package hr.algebra.azul;

import hr.algebra.azul.controllers.ModernMenuController;
import hr.algebra.azul.view.ModernMenuView;
import javafx.application.Application;
import javafx.stage.Stage;

public class AzulApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        ModernMenuView menuView = new ModernMenuView();
        ModernMenuController menuController = new ModernMenuController(menuView, primaryStage);
        menuController.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
