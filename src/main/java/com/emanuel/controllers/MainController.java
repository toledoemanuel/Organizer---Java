package com.emanuel.controllers;

import com.emanuel.Contact; 
import com.emanuel.Appointment;
import com.emanuel.Organizer; 
import com.emanuel.CSVManager;
import com.emanuel.exceptions.DuplicateItemException;
import com.emanuel.exceptions.ItemNotFoundException;
import com.emanuel.utils.DialogUtils;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainController 
{
    private static Organizer<Contact> contactOrganizer = new Organizer<>();
    private static Organizer<Appointment> appointmentOrganizer = new Organizer<>();

    private static CSVManager csvManager = new CSVManager();
    private static boolean isDataLoaded = false;

    @FXML
    public void initialize() {
        if (!isDataLoaded) {
            try 
            {
                for (Contact c : csvManager.loadContacts()) 
                {
                    contactOrganizer.add(c);
                }

                for (Appointment a : csvManager.loadAppointments()) 
                {
                    appointmentOrganizer.add(a);
                }
                
                isDataLoaded = true; 

                List<String> warnings = csvManager.getAndClearWarnings();
                if (!warnings.isEmpty())
                {
                    StringBuilder warningMessage = new StringBuilder("The following corrupted items were skipped and removed:\n\n");
                    for (String w : warnings)
                    {
                        warningMessage.append(" - ").append(w).append("\n");
                    }
                    DialogUtils.showWarningAlert("Warning: Corrupted Data Detected ", warningMessage.toString());
                }
                System.out.println("Data loaded successfully.");

            } 
            
            catch (DuplicateItemException e) 
            {
                System.out.println("Notice: Skipped duplicate item during load - " + e.getMessage());
                isDataLoaded = true;
            }
        }
    }

    @FXML
    public void onContactsButtonClick(ActionEvent event) 
    {
        openManagementView(event, "Contacts", contactOrganizer);
    }

    @FXML
    public void onAppointmentsButtonClick(ActionEvent event) 
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<String> oldAppointments = new ArrayList<>();

        for (Appointment apt : appointmentOrganizer.getAllItems())
        {
            try
            {
                LocalDate aptDate = LocalDate.parse(apt.getDate(), formatter);
    
                if (aptDate.isBefore(LocalDate.now()))
                {
                    oldAppointments.add(apt.getIdentifier());
                }
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Warning: Skipped unparseable date for appointment " + apt.getTitle() + "-" + apt.getDate());
            }
        }

        if (!oldAppointments.isEmpty())
        {
            for (String id : oldAppointments)
            {
                try
                {
                    appointmentOrganizer.remove(id);
                }
                catch (ItemNotFoundException e)
                {
                    System.out.println("Error removing old appointments: " + e.getMessage());
                }

                DialogUtils.showAlert("System notice", oldAppointments.size() + " past appointments were automatically removed");
            }
        }

        openManagementView(event, "Appointments", appointmentOrganizer);
    }

    public static void saveAllData() 
    {
        csvManager.saveContacts(contactOrganizer.getAllItems());
        csvManager.saveAppointments(appointmentOrganizer.getAllItems());
    }

    @FXML
    public void onExitButtonClick() 
    {
        saveAllData();

        csvManager.saveContacts(contactOrganizer.getAllItems());
        csvManager.saveAppointments(appointmentOrganizer.getAllItems());

        DialogUtils.showTemporaryAlert("Data saved successfully. Exiting.", null, () -> System.exit(0));
    }

    private void openManagementView(ActionEvent event, String type, Organizer<?> organizer) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emanuel/ManagementView.fxml"));
            Parent root = loader.load();

            ManagementController controller = loader.getController();
            controller.initData(type, organizer);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            stage.setMinWidth(stage.getWidth() - stage.getScene().getWidth() + root.minWidth(-1));
            stage.setMinHeight(stage.getHeight() - stage.getScene().getHeight() + root.minHeight(-1));
        } 
        
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}