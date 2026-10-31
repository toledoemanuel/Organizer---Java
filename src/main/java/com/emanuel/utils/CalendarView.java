package com.emanuel.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.emanuel.Appointment;
import com.emanuel.Schedulable;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class CalendarView extends VBox
{
    private static final String[] WEEKDAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final DateTimeFormatter MONTH_FORMAT =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final Label monthLabel = new Label();
    private final GridPane grid = new GridPane();

    private YearMonth currentMonth = YearMonth.now();
    private Map<LocalDate, List<Appointment>> appointmentsByDay = new HashMap<>();

    public CalendarView()
    {
        setSpacing(12);

        Button prevButton = navButton("‹");
        prevButton.setOnAction(e -> { currentMonth = currentMonth.minusMonths(1); render(); });

        Button nextButton = navButton("›");
        nextButton.setOnAction(e -> { currentMonth = currentMonth.plusMonths(1); render(); });

        Button todayButton = navButton("Today");
        todayButton.setOnAction(e -> { currentMonth = YearMonth.now(); render(); });

        monthLabel.getStyleClass().add("calendar-title");
        monthLabel.setMinWidth(150);
        monthLabel.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, prevButton, monthLabel, nextButton, spacer, todayButton);
        header.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(4);
        grid.setVgap(4);

        for (int i = 0; i < 7; i++)
        {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7);
            grid.getColumnConstraints().add(column);
        }

        grid.getRowConstraints().add(new RowConstraints());

        for (int i = 0; i < 6; i++)
        {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(48);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }

        VBox.setVgrow(grid, Priority.ALWAYS);
        getChildren().addAll(header, grid);

        render();
    }

    public void setAppointments(Collection<? extends Schedulable> items)
    {
        appointmentsByDay = new HashMap<>();

        for (Schedulable item : items)
        {
            if (item instanceof Appointment a)
            {
                appointmentsByDay
                    .computeIfAbsent(a.getDateTime().toLocalDate(), d -> new ArrayList<>())
                    .add(a);
            }
        }

        render();
    }

    private Button navButton(String text)
    {
        Button button = new Button(text);
        button.getStyleClass().add("calendar-nav");
        return button;
    }

    private void render()
    {
        grid.getChildren().clear();
        monthLabel.setText(currentMonth.format(MONTH_FORMAT));

        for (int col = 0; col < 7; col++)
        {
            Label weekday = new Label(WEEKDAYS[col]);
            weekday.getStyleClass().add("calendar-weekday");
            GridPane.setHalignment(weekday, HPos.CENTER);
            grid.add(weekday, col, 0);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int offset = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate start = firstOfMonth.minusDays(offset);
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 42; i++)
        {
            LocalDate date = start.plusDays(i);
            grid.add(buildCell(date, today), i % 7, i / 7 + 1);
        }
    }

    private VBox buildCell(LocalDate date, LocalDate today)
    {
        VBox cell = new VBox(4);
        cell.getStyleClass().add("calendar-cell");
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("calendar-day");
        cell.getChildren().add(dayLabel);

        if (!YearMonth.from(date).equals(currentMonth))
        {
            cell.getStyleClass().add("calendar-cell-other");
        }

        if (date.equals(today))
        {
            cell.getStyleClass().add("calendar-cell-today");
        }

        List<Appointment> dayAppointments = appointmentsByDay.get(date);

        if (dayAppointments != null && !dayAppointments.isEmpty())
        {
            cell.getStyleClass().add("calendar-cell-busy");

            Label badge = new Label(String.valueOf(dayAppointments.size()));
            badge.getStyleClass().add("calendar-badge");
            cell.getChildren().add(badge);

            StringBuilder text = new StringBuilder();
            dayAppointments.stream()
                .sorted(Comparator.comparing(Appointment::getTime))
                .forEach(a -> text.append(a.getTime()).append("  ").append(a.getTitle()).append("\n"));

            Tooltip.install(cell, new Tooltip(text.toString().trim()));
        }

        return cell;
    }
}