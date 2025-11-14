/***********************************************************************************************************************
 * JavaFX controller that handles the view of the table of forms
 * Allows opening of forms and quick actions from the table view
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FormTableController {

    // Switch to detailed form view with filled fields
    @FXML
    private void openForm() throws IOException {
        
    }

    @FXML
    private Label dbStatus; // Shows "DB: connected (...)" or error message. **FOR TESTING - REMOVE LATER**

    /** ***FOR TESTING - REMOVE LATER***
     *  Handler for the "Test DB Connection" button in main.fxml.     * 
     *
     *  - Opens a connection using TCEPDatabaseService (so we use the same settings everywhere).
     *  - Runs a SELECT COUNT(*) on TCEP_Form: shows we can reach our actual app table.
     *  - Runs SHOW TABLES: shows the schema exists even if the form table is still empty.
     *  - Displays the result in the UI label so teammates/sponsor can see it without looking at console.
     *
     *  - This is a quick integration check between the JavaFX front end and the XAMPP/MySQL backend.
     *  - Our TCEP_Form table might be empty during early development, so we also count tables to show “real” output.
     * 
     *  Jeffrey Chou
     */
    @FXML
    public void initialize() {
        // 1. bind columns to TCEPForm getters
        studentNameCol.setCellValueFactory(cellData -> cellData.getValue().getStudentNameProperty());
        utdIdCol.setCellValueFactory(cellData -> cellData.getValue().getUtdIdProperty());
        netIdCol.setCellValueFactory(cellData -> cellData.getValue().getNetIdProperty());
        dateStartedCol.setCellValueFactory(cellData -> cellData.getValue().getStartedDateProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().getStatusProperty());
        
        // 2. bind form table to TCEPForm table
        formTable.setRowFactory(table -> {
            TableRow<TCEPForm> row = new TableRow<>();

            if (row.getItem() != null)
            {
                row.setCursor(Cursor.HAND);
            }

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && row.getItem() != null)
                {
                    formTable.fireEvent(new NavigationRequestEvent(row.getItem()));
                }
            });

            return row;
        });

        filteredData = new FilteredList<>(masterData, p -> true); 
        formTable.setItems(filteredData);

        // 3. load data from DB
        try {
            formTableObject.loadForms();
            masterData.clear();
            masterData.addAll(formTableObject.rows);

            if (dbStatus != null) {
                dbStatus.setText("DB: ✅ loaded " + formTableObject.rows.size() + " form(s)");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (dbStatus != null) {
                dbStatus.setText("DB: ❌ " + e.getMessage());
            }
        }
    }

    // Handles refresh button. Calls loadForms to re-query the DB.
    // Written by Jeffrey Chou (jxc033200)
    @FXML
    private void onRefreshClicked() {
        try {
            formTableObject.loadForms();
        } catch (Exception e) {
            dbStatus.setText("DB: ❌ not connected");
            e.printStackTrace();
        }
    }
    
}
 