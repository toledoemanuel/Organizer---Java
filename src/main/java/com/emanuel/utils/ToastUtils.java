package com.emanuel.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class ToastUtils
{
    private enum Type
    {
        SUCCESS("#27ae60", 2.5),
        ERROR("#c0392b", 4.0),
        INFO("#3c3f41", 3.0);

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

    public static void success(Node anchor, String message)
    {
        show(anchor, message, Type.SUCCESS);
    }

    public static void error(Node anchor, String message)
    {
        show(anchor, message, Type.ERROR);
    }

    public static void info(Node anchor, String message)
    {
        show(anchor, message, Type.INFO);
    }

    private static void show(Node anchor, String message, Type type)
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
        label.setStyle(
            "-fx-background-color: " + type.color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 20 12 20;" +
            "-fx-background-radius: 8;");

        StackPane box = new StackPane(label);
        box.setStyle("-fx-padding: 12;");
        label.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.4)));
        box.setOpacity(0);

        Popup popup = new Popup();
        popup.getContent().add(box);

        popup.setOnShown(e -> {
            popup.setX(owner.getX() + owner.getWidth() - popup.getWidth() - 12);
            popup.setY(owner.getY() + owner.getHeight() - popup.getHeight() - 12);
        });

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), box);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(type.seconds));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), box);
        fadeOut.setToValue(0);

        SequentialTransition animation = new SequentialTransition(fadeIn, stay, fadeOut);
        animation.setOnFinished(e -> popup.hide());

        currentPopup = popup;
        currentAnimation = animation;

        popup.show(owner);
        animation.play();
    }

    private static void dismissCurrent()
    {
        if (currentAnimation != null)
        {
            currentAnimation.stop();
        }

        if (currentPopup != null)
        {
            currentPopup.hide();
        }
    }
}
