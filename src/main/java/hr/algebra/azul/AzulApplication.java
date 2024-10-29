package hr.algebra.azul;

import hr.algebra.azul.services.GameStateManager;
import hr.algebra.azul.services.NavigationService;
import hr.algebra.azul.services.ResourceManager;
import hr.algebra.azul.services.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main entry point for the Azul game application.
 * Manages the application lifecycle and core services.
 */

public class AzulApplication extends Application {
    private ResourceManager resourceManager;
    private SceneManager sceneManager;
    private NavigationService navigationService;
    private GameStateManager gameStateManager;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        try {
            initializeServices();
            configureStage();
            setupInitialScene();
            registerShutdownHook();
            primaryStage.show();
        }catch (Exception e){
            System.err.println("Failed to start the application.");
            Platform.exit();
        }
    }

    private void initializeServices(Stage primaryStage) {
        // Initialize core services in the correct order
        resourceManager = new ResourceManager();
        resourceManager.initializeResources();

        sceneManager = new SceneManager(primaryStage);

        navigationService = new NavigationService(sceneManager);

        gameStateManager = new GameStateManager();

        // Register the services for dependency injection if needed
        // This could be replaced with a proper DI framework later
        ServiceLocator.registerService(resourceManager);

    }

    private void setupInitialScene() {
        sceneManager.switchScene(SceneType.MENU);
    }

    public void Main(String[] args) {
        launch(args);
    }
}
