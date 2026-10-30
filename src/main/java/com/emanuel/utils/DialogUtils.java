package com.emanuel.utils;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DialogUtils 
{
    public static String askInput(String title, String content) 
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); 
    }

    public static String askInput(String title, String content, String defaultValue) 
    {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); 
    }

    public static void showSuccessAlert(String message) 
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> alert.close());

        alert.show();
        delay.play();
    }

    public static void showAlert(String title, String content) 
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static String askDateInput(String title, String content, String defaultValue)
    {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(content);
        TextField textField = new TextField(defaultValue);
        Button todayButton = new Button("Today");

        todayButton.setOnAction(e -> {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            textField.setText(today);
        });

        hbox.getChildren().addAll(label, textField, todayButton);
        dialog.getDialogPane().setContent(hbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return textField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
