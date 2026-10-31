package com.emanuel.utils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class ToastUtils
{
    private enum Type
    {
        SUCCESS("#27ae60", 2.5),
        ERROR("#c0392b", 4.0),
        INFO("#3c3f41", 3.0),
        UNDO("#3c3f41", 6.0);

        private final String color;
        private final double seconds;

        Type(String color, double seconds)
        {
            this.color = color;
            this.seconds = seconds;
        }
    }

    private static Popup currentPopup;
    private static SequentialTransition currentAnimation;
    private static Timeline currentCountdown;

    public static void success(Node anchor, String message)
    {
        show(anchor, message, Type.SUCCESS, null, null);
    }

    public static void error(Node anchor, String message)
    {
        show(anchor, message, Type.ERROR, null, null);
    }

    public static void info(Node anchor, String message)
    {
        show(anchor, message, Type.INFO, null, null);
    }

    public static void undo(Node anchor, String message, Runnable onUndo)
    {
        show(anchor, message, Type.UNDO, "Undo", onUndo);
    }

    private static void show(Node anchor, String message, Type type, String actionText, Runnable onAction)
    {
        if (anchor == null || anchor.getScene() == null || anchor.getScene().getWindow() == null)
        {
            return;
        }

        Window owner = anchor.getScene().getWindow();

        dismissCurrent();

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(320);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox row = new HBox(16, label);
        row.setAlignment(Pos.CENTER_LEFT);

        if (actionText != null)
        {
            Button actionButton = new Button(actionText);
            actionButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #5bc0de;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 0;" +
                "-fx-cursor: hand;");

            actionButton.setOnAction(e -> {
                dismissCurrent();   
                onAction.run();
            });

            row.getChildren().add(actionButton);
        }

        VBox content = new VBox(10, row);
        content.setStyle(
            "-fx-background-color: " + type.color + ";" +
            "-fx-padding: 12 20 12 20;" +
            "-fx-background-radius: 8;");
        content.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.4)));

        Timeline countdown = null;

        if (actionText != null)
        {
            Region bar = new Region();
            bar.setPrefHeight(3);
            bar.setMinHeight(3);
            bar.setMaxHeight(3);
            bar.setMaxWidth(Double.MAX_VALUE); 
            bar.setStyle("-fx-background-color: #5bc0de; -fx-background-radius: 2;");

            Scale scale = new Scale(1, 1, 0, 0);
            bar.getTransforms().add(scale);

            countdown = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scale.xProperty(), 1)),
                new KeyFrame(Duration.seconds(type.seconds), new KeyValue(scale.xProperty(), 0)));

            content.getChildren().add(bar);
        }

        StackPane box = new StackPane(content);
        box.setStyle("-fx-padding: 12;");
        box.setOpacity(0);

        Popup popup = new Popup();
        popup.getContent().add(box);

        popup.setOnShown(e -> {
            popup.setX(owner.getX() + owner.getWidth() - popup.getWidth() - 12);
            popup.setY(owner.getY() + owner.getHeight() - popup.getHeight() - 12);
        });

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), box);
        fadeIn.setToValue(1);

        final Timeline barAnimation = countdown;
        if (barAnimation != null)
        {
            fadeIn.setOnFinished(e -> barAnimation.play());
        }

        PauseTransition stay = new PauseTransition(Duration.seconds(type.seconds));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), box);
        fadeOut.setToValue(0);

        SequentialTransition animation = new SequentialTransition(fadeIn, stay, fadeOut);
        animation.setOnFinished(e -> popup.hide());

        currentPopup = popup;
        currentAnimation = animation;
        currentCountdown = countdown;

        popup.show(owner);
        animation.play();
    }

    private static void dismissCurrent()
    {
        if (currentAnimation != null)
        {
            currentAnimation.stop();
        }

        if (currentCountdown != null)
        {
            currentCountdown.stop();
            currentCountdown = null;
        }

        if (currentPopup != null)
        {
            currentPopup.hide();
        }
    }
}