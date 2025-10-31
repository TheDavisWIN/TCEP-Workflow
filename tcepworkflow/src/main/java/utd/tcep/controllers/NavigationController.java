/***********************************************************************************************************************
 * Handles navigation bar
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class NavigationController {

    // Show the full form table
    @FXML
    private void handleShowFormTable() throws IOException {
        
    }

    // Create a new form with blank fields
    @FXML
    private void handleShowBlankForm() throws IOException {
        
    }

    @FXML
    private void handleLogout() throws IOException {
        
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
     */
    @FXML
    private void onTestDbClicked() {
        String formCountSql = "SELECT COUNT(*) AS cnt FROM TCEP_Form";
        String tableCountSql = "SHOW TABLES";

        try (Connection conn = utd.tcep.db.TCEPDatabaseService.getConnection();
            PreparedStatement formPs = conn.prepareStatement(formCountSql);
            PreparedStatement tablePs = conn.prepareStatement(tableCountSql)) {

            // Get form count
            int formCount = 0;
            try (ResultSet rs = formPs.executeQuery()) {
                if (rs.next()) formCount = rs.getInt("cnt");
            }

            // Get total number of tables
            int tableCount = 0;
            try (ResultSet rs = tablePs.executeQuery()) {
                while (rs.next()) tableCount++;
            }

            // Display both results
            String message = String.format("DB: ✅ connected (%d forms, %d tables)", formCount, tableCount);
            dbStatus.setText(message);
            System.out.println("✅ Database connected! " + message);

        } catch (Exception e) {
            dbStatus.setText("DB: ❌ not connected");
            e.printStackTrace();
        }
    }
}
 