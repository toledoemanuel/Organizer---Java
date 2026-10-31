package com.emanuel.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;

import java.io.IOException;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.Button;

import com.emanuel.Contact;
import com.emanuel.Appointment;
import com.emanuel.Organizer;
import com.emanuel.Schedulable;
import com.emanuel.exceptions.DuplicateItemException;
import com.emanuel.exceptions.InvalidItemException;
import com.emanuel.exceptions.ItemNotFoundException;
import com.emanuel.utils.DialogUtils;
import com.emanuel.utils.SearchFilter;
import com.emanuel.utils.SortUtils;
import com.emanuel.utils.ToastUtils;

public class ManagementController {

    @FXML 
    private Label sectionTitleLabel;
    
    @FXML 
    private ListView<Schedulable> recordsListView; 

    private Organizer<Schedulable> currentOrganizer; 
    private ObservableList<Schedulable> uiList; 
    private String currentType;

    @FXML
    private Button sortButton;

    private SortedList<Schedulable> sortedData;
    private boolean ascending = true;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initData(String type, Organizer<?> organizer) 
    {
        this.currentType = type;
        this.currentOrganizer = (Organizer<Schedulable>) (Organizer) organizer; 
        this.sectionTitleLabel.setText(type);
        
        this.uiList = FXCollections.observableArrayList(this.currentOrganizer.getAllItems());

        FilteredList<Schedulable> filteredData = new FilteredList<>(uiList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> 
            filteredData.setPredicate(item -> SearchFilter.matches(item, newValue)));

        this.sortedData = new SortedList<>(filteredData);
        this.recordsListView.setItems(sortedData);

        applySort();

        // Update on mouse double-click 
        this.recordsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
            {
                performUpdate();
            }
        });
    }

    private void refreshUI()
    {
        this.uiList.setAll(this.currentOrganizer.getAllItems());
    }

    private void applySort()
    {
        if (currentType.equals("Contacts"))
        {
            sortedData.setComparator(SortUtils.byName(ascending));
            sortButton.setText(ascending ? "A → Z" : "Z → A");
        }
        else
        {
            sortedData.setComparator(SortUtils.byDate(ascending));
            sortButton.setText(ascending ? "Nearest first" : "Furthest first");
        }
    }

    @FXML
    public void onSortToggleClick(ActionEvent event)
    {
        ascending = !ascending;
        applySort();
    }

    @FXML 
    private TextField searchField;

    @FXML
    public void onAddClick(ActionEvent event) 
    {
        if (currentType.equals("Contacts"))
        {
            String name = DialogUtils.askInput("Add Contact", "Name:");
            if (name == null) return; 
            
            String phone = DialogUtils.askInput("Add Contact", "Phone:");
            if (phone == null) return; 

            try 
            {
                Contact newContact = new Contact(name, phone);
                currentOrganizer.add(newContact); 
                ToastUtils.success(recordsListView, "Contact added successfully");
                refreshUI(); 
            } 
            
            catch (InvalidItemException | DuplicateItemException e) 
            { 
                ToastUtils.error(recordsListView, e.getMessage());
            }
            
        } 
        else if (currentType.equals("Appointments"))
        {
            String title = DialogUtils.askInput("Add Appointment", "Title:");
            if (title == null) return;

            String date = DialogUtils.askDateInput("Add Appointment", "Date (e.g., DD/MM/YYYY):", "");
            if (date == null) return;

            String time = DialogUtils.askInput("Add Appointment", "Time (e.g., HH:MM):");
            if (time == null) return;

            try 
            {
                Appointment newAppointment = new Appointment(title, date, time);
                currentOrganizer.add(newAppointment);
                ToastUtils.success(recordsListView, "Appointment added successfully");
                refreshUI();
            } 
            
            catch (InvalidItemException | DuplicateItemException e) 
            {
                ToastUtils.error(recordsListView, e.getMessage());
            }
        }
    }

    @FXML
    public void onRemoveClick(ActionEvent event) 
    {
        Schedulable selectedItem = recordsListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) 
        {
            try 
            {
                currentOrganizer.remove(selectedItem.getIdentifier());
                ToastUtils.success(recordsListView,"Item removed successfully");
                refreshUI();
            } 
            
            catch (ItemNotFoundException e) 
            {
                ToastUtils.error(recordsListView, e.getMessage());
            }
        } 
        else
        {
            ToastUtils.info(recordsListView, "Please select an item to remove.");
        }
    }

    @FXML
    public void onUpdateClick(ActionEvent event) 
    {
        performUpdate();
    }

    private void performUpdate() 
    {
        Schedulable selectedItem = recordsListView.getSelectionModel().getSelectedItem();
        
        if (selectedItem != null) 
        {
            if (currentType.equals("Contacts")) 
            {
                Contact currentContact = (Contact) selectedItem;

                String name = DialogUtils.askInput("Update Contact", "Name:", currentContact.getName());
                if (name == null) return;
                
                String phone = DialogUtils.askInput("Update Contact", "Phone:", currentContact.getPhone());
                if (phone == null) return;

                try 
                {
                    Contact updatedContact = new Contact(name, phone);
                    currentOrganizer.update(selectedItem.getIdentifier(), updatedContact);
                    ToastUtils.success(recordsListView, "Contact updated successfully");
                    refreshUI();
                } 
                
                catch (InvalidItemException | ItemNotFoundException e) 
                {
                    ToastUtils.error(recordsListView, e.getMessage());
                }
            } 
            else if (currentType.equals("Appointments")) 
            {
                Appointment currentAppointment = (Appointment) selectedItem;

                String title = DialogUtils.askInput("Update Appointment", "Title:", currentAppointment.getTitle());
                if (title == null) return;
                
                String date = DialogUtils.askDateInput("Update Appointment", "Date:", currentAppointment.getDate());
                if (date == null) return;
                
                String time = DialogUtils.askInput("Update Appointment", "Time:", currentAppointment.getTime());
                if (time == null) return;

                try 
                {
                    Appointment updatedAppointment = new Appointment(title, date, time);
                    currentOrganizer.update(selectedItem.getIdentifier(), updatedAppointment);
                    ToastUtils.success(recordsListView, "Appointment updated successfully");
                    refreshUI();
                } 
                
                catch (InvalidItemException | ItemNotFoundException e)
                {
                    ToastUtils.error(recordsListView, e.getMessage());
                }
            }
        } 
        else 
        {
            ToastUtils.info(recordsListView, "Please select an item to update.");
        }
    }

    @FXML
    public void onBackToMenuClick(ActionEvent event) 
    {
        try 
        {
            Parent root = FXMLLoader.load(getClass().getResource("/com/emanuel/MainView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackgroundClick(javafx.scene.input.MouseEvent event)
    {
        recordsListView.getSelectionModel().clearSelection();
    }
}