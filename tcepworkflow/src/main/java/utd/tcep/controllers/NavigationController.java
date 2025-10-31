/***********************************************************************************************************************
 * Handles navigation bar
 * Ryan Pham
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import utd.tcep.data.TCEPForm;
import utd.tcep.main.TCEPWorkflowApp;

public class NavigationController {

    @FXML
    private GridPane appGridPane;
    private Node currentView;

    // Show the full form table
    // Ryan Pham
    @FXML
    private void handleShowFormTable() throws IOException {
        swapView("/utd/tcep/formtableview");
    }

    // Create a new form with blank fields
    // Ryan Pham
    @FXML
    private void handleShowBlankForm() throws IOException {
        swapView("/utd/tcep/formdetailedview");

        FormDetailedController.currentForm = new TCEPForm();
    }

    // Ryan Pham
    @FXML
    private void handleLogout() throws IOException {
        System.out.println("Logout");
    }

    // Swap between different views and load FXML when navigation buttons are clicked
    // Ryan Pham
    private void swapView(String fxml) throws IOException {
        if (currentView != null)
        {
            appGridPane.getChildren().remove(currentView);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource(fxml + ".fxml"));
        currentView = fxmlLoader.load();
        appGridPane.getChildren().add(currentView);
        GridPane.setColumnIndex(currentView, 1);
    }
}
 