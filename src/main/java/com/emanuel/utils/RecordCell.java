package com.emanuel.utils;

import com.emanuel.Appointment;
import com.emanuel.Contact;
import com.emanuel.Schedulable;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RecordCell extends ListCell<Schedulable>
{
    @Override
    protected void updateItem(Schedulable item, boolean empty)
    {
        super.updateItem(item, empty);

        if (empty || item == null)
        {
            setText(null);
            setGraphic(null);
            return;
        }

        if (item instanceof Contact c)
        {
            setText(null);
            setGraphic(build(initials(c.getName()), c.getName(), c.getPhone()));
        }
        else if (item instanceof Appointment a)
        {
            setText(null);
            setGraphic(build(a.getDate().substring(0, 2), a.getTitle(), a.getDate() + " at " + a.getTime()));
        }
        else
        {
            setText(item.getDetails());
            setGraphic(null);
        }
    }

    private HBox build(String avatarText, String title, String subtitle)
    {
        Label avatarLabel = new Label(avatarText);
        avatarLabel.getStyleClass().add("item-avatar-text");

        StackPane avatar = new StackPane(avatarLabel);
        avatar.getStyleClass().add("item-avatar");
        avatar.setMinSize(40, 40);
        avatar.setPrefSize(40, 40);
        avatar.setMaxSize(40, 40);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("item-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("item-subtitle");

        VBox texts = new VBox(2, titleLabel, subtitleLabel);
        texts.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12, avatar, texts);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String initials(String name)
    {
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);

        if (parts.length == 1)
        {
            return first.toUpperCase();
        }

        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }
}