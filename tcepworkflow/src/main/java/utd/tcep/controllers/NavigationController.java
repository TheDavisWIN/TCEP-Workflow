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
    private Node formDetailedView;
    private Node formTableView;
    private Node loginView;
    @FXML
    private VBox navigationBar;
    private FormDetailedController formDetailedController;
    private FormTableController formTableController;
    private LoginController loginController;

    // Automatically called on program start, saving controllers for future method calls
    // Ryan Pham (rkp200003)
    // Davis Huynh (dxh170005) (added Login view)
    @FXML
    public void initialize() throws IOException {
        FXMLLoader formDetailedViewLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdetailedview.fxml"));
        FXMLLoader formTableViewLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formtableview.fxml"));
        FXMLLoader loginViewLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/loginview.fxml"));
        formDetailedView = formDetailedViewLoader.load();
        formTableView = formTableViewLoader.load();
        loginView = loginViewLoader.load();
        appGridPane.getChildren().add(loginView);
        appGridPane.getChildren().add(formDetailedView);
        appGridPane.getChildren().add(formTableView);
        GridPane.setColumnIndex(loginView, 1);
        GridPane.setColumnIndex(formDetailedView, 1);
        GridPane.setColumnIndex(formTableView, 1);
        formDetailedController = formDetailedViewLoader.getController();
        formTableController = formTableViewLoader.getController();
        loginController = loginViewLoader.getController();
        loginController.setNavigationController(this);

        swapView(View.Login);

        // Handle when user left clicks on a row in the form table in order to open the form
        formTableController.formTable.addEventHandler(NavigationRequestEvent.REQUEST, event -> {
            swapView(View.Detailed);
            formDetailedController.setForm(event.getForm());
        });
    }

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
        swapView(View.Detailed);
        formDetailedController.setForm(formTableController.getFormTableObject().createBlankForm());
        formTableController.formTable.refresh();
        System.out.println(formTableController.getFormTableObject().rows.size());
    }

    // Ryan Pham
    @FXML
    private void handleLogout() throws IOException {
        System.out.println("Logout");
    }

    // Swap between different views and load FXML when navigation buttons are clicked
    // Ryan Pham (rkp200003)
    // Davis Huynh (dxh170005) (added Login view)
    public void swapView(View view) {
        loginView.setVisible(false);
        formDetailedView.setVisible(false);
        formTableView.setVisible(false);

        switch (view) {
            case Login:
                loginView.setVisible(true);
                break;
            case Detailed:
                formDetailedView.setVisible(true);
                break;
            case Table:
                formTableView.setVisible(true);
                break;
        }
    }
}
 
