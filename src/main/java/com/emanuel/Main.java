package com.emanuel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) 
        throws Exception {
            
        Parent root = FXMLLoader.load(getClass().getResource("/com/emanuel/MainView.fxml"));

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/com/emanuel/style.css").toExternalForm());

        primaryStage.setOnCloseRequest(event -> {com.emanuel.controllers.MainController.saveAllData();});

        primaryStage.setTitle("Organizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setMinWidth(primaryStage.getWidth() - scene.getWidth() + root.minWidth(-1));
        primaryStage.setMinHeight(primaryStage.getHeight() - scene.getHeight() + root.minHeight(-1));
    }

    public static void main(String[] args) {
        launch(args);
    }
}