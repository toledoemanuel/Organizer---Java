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

import java.util.function.Consumer;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    private static final DateTimeFormatter DETAIL_FORMAT =
        DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.ENGLISH);

    private final Label monthLabel = new Label();
    private final GridPane grid = new GridPane();
    private final VBox detailBox = new VBox(6);

    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate = null;
    private Map<LocalDate, List<Appointment>> appointmentsByDay = new HashMap<>();

    private final Map<LocalDate, VBox> cellsByDate = new HashMap<>();
    private Consumer<LocalDate> onDayDoubleClick;

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
            row.setMinHeight(36);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }

        VBox.setVgrow(grid, Priority.ALWAYS);

        detailBox.getStyleClass().add("calendar-detail-box");

        ScrollPane detailScroll = new ScrollPane(detailBox);
        detailScroll.setFitToWidth(true);
        detailScroll.setMinHeight(90);
        detailScroll.setPrefHeight(100);
        detailScroll.setMaxHeight(120);
        detailScroll.getStyleClass().add("calendar-detail");

        getChildren().addAll(header, grid, detailScroll);

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
        cellsByDate.clear();
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

        renderDetails();
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

        if (date.equals(selectedDate))
        {
            cell.getStyleClass().add("calendar-cell-selected");
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

        cellsByDate.put(date, cell);

        cell.setOnMouseClicked(e -> {
            selectDay(date);

            if (e.getClickCount() == 2 && onDayDoubleClick != null)
            {
                onDayDoubleClick.accept(date);
            }

            e.consume();
        });

        return cell;
    }

    private void selectDay(LocalDate date)
    {
        VBox previous = cellsByDate.get(selectedDate);
        if (previous != null)
        {
            previous.getStyleClass().remove("calendar-cell-selected");
        }

        selectedDate = date;

        VBox current = cellsByDate.get(date);
        if (current != null && !current.getStyleClass().contains("calendar-cell-selected"))
        {
            current.getStyleClass().add("calendar-cell-selected");
        }

        renderDetails();
    }

    private void renderDetails()
    {
        detailBox.getChildren().clear();

        if (selectedDate == null)
        {
            detailBox.getChildren().add(detailLabel("Click a day to see its appointments.", "calendar-detail-hint"));
            return;
        }

        detailBox.getChildren().add(detailLabel(selectedDate.format(DETAIL_FORMAT), "calendar-detail-title"));

        List<Appointment> dayAppointments = appointmentsByDay.get(selectedDate);

        if (dayAppointments == null || dayAppointments.isEmpty())
        {
            detailBox.getChildren().add(detailLabel("No appointments on this day.", "calendar-detail-hint"));
            return;
        }

        dayAppointments.stream()
            .sorted(Comparator.comparing(Appointment::getTime))
            .forEach(a ->
            {
                Label time = detailLabel(a.getTime(), "calendar-detail-time");
                time.setMinWidth(50);

                Label title = detailLabel(a.getTitle(), "calendar-detail-item");

                HBox row = new HBox(10, time, title);
                row.setAlignment(Pos.CENTER_LEFT);
                detailBox.getChildren().add(row);
            });
    }

    private Label detailLabel(String text, String styleClass)
    {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    public void setOnDayDoubleClick(Consumer<LocalDate> handler)
    {
        this.onDayDoubleClick = handler;
    }


}