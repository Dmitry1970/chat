package ru.javachat2;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = FXMLLoader.load(getClass().getResource("/chat2.fxml"));
        Controller controller = fxmlLoader.getController();

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Chat");
        primaryStage.setOnCloseRequest(event -> controller.sendCloseRequest());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
