/***********************************************************************************************************************
 * Handles navigation bar
 * Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import utd.tcep.data.TCEPUser;
import utd.tcep.events.NavigationRequestEvent;
import utd.tcep.main.TCEPWorkflowApp;
import utd.tcep.db.TCEPDatabaseService;


public class NavigationController {

    public enum View {
        Detailed,
        Table,
        Login
    }

    @FXML
    private GridPane appGridPane;
    private Node formDetailedView;
    private Node formTableView;
    private Node loginView;
    @FXML
    private VBox navigationBar;
    @FXML
    private Label loggedInAdvisorLabel;
    @FXML
    private Label loggedInAdvisorIdLabel;
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
        // Give detailed controller reference to navigation
        formDetailedController.setNavigationController(this);
    
        
        // Set references in FormDetailedController
        formDetailedController.setFormTableController(formTableController);
        formDetailedController.setNavigationController(this);
        
        // Set callback to refresh table when status changes in detailed view
        formDetailedController.setOnStatusChangeCallback(() -> {
            formTableController.refreshTable();
        });
        
        // Set callback to navigate to table view after deletion
        formDetailedController.setOnNavigateToTableCallback(() -> {
            swapView(View.Table);
        });

        swapView(View.Login);

        // Handle when user left clicks on a row in the form table in order to open the form
        formTableController.formTable.addEventHandler(NavigationRequestEvent.REQUEST, event -> {
            swapView(View.Detailed);
            formDetailedController.setForm(event.getForm());
        });
    }

    // Show the full form table
    // Ryan Pham (rkp200003)
    @FXML
    private void handleShowFormTable() throws IOException {
        swapView(View.Table);
        formTableController.refreshMasterData();
    }

    // Create a new form with blank fields
    // Ryan Pham (rkp200003)
    @FXML
    private void handleShowBlankForm() throws IOException {
        swapView(View.Detailed);
        formDetailedController.setForm(formTableController.getFormTableObject().createBlankForm());
    }

    // Ryan Pham (rkp200003)
    // Davis Huynh (dxh170005) (added logout functionality)
    // Ayden Benel (acb210001) (added database closing)
    @FXML
    private void handleLogout() throws IOException {
        swapView(View.Login);
        navigationBar.setVisible(false);
        System.out.println("Logout");
        TCEPDatabaseService.closeConnection();
        loginController.resetFields();
    }

    // Called when login is successful
    // Davis Huynh (dxh170005)
    public void onLoginSuccess() {
        // Update advisor info from current user session
        TCEPUser currentUser = TCEPUser.getCurrentUser();
        if (currentUser != null) {
            if (loggedInAdvisorLabel != null) {
                loggedInAdvisorLabel.setText(currentUser.getAdvisorName() != null ? currentUser.getAdvisorName() : "Unknown");
            }
            if (loggedInAdvisorIdLabel != null) {
                loggedInAdvisorIdLabel.setText("ID: " + currentUser.getAdvisorId());
            }
            // Set current user in FormDetailedController
            formDetailedController.setCurrentUser(currentUser);
            // Set current advisor in FormTableController to filter forms
            formTableController.setCurrentAdvisor(currentUser.getAdvisorId());
            // Refresh table to load forms for this advisor
            formTableController.refreshTable();
        }
        
        navigationBar.setVisible(true);
        swapView(View.Table);
    }

    // Show or hide the navigation bar
    // Davis Huynh (dxh170005)
    public void showNavigationBar(boolean show) {
        if (navigationBar != null) {
            navigationBar.setVisible(show);
        }
    }

    // Swap between different views and load FXML when navigation buttons are clicked
    // Ryan Pham (rkp200003)
    // Davis Huynh (dxh170005) (added Login view)
  public void swapView(View view) {
    // Always clear column 1 first — THIS IS THE KEY FIX
    appGridPane.getChildren().removeIf(node -> {
        Integer col = GridPane.getColumnIndex(node);
        return col != null && col == 1;
    });

    // Reset edit mode when leaving detailed view
    if (view == View.Table || view == View.Login) {
        formDetailedController.resetEditMode();
    }

    loginView.setVisible(false);
    formDetailedView.setVisible(false);
    formTableView.setVisible(false);

    switch (view) {
        case Login:
            loginView.setVisible(true);
            appGridPane.add(loginView, 1, 0);
            break;
        case Detailed:
            formDetailedView.setVisible(true);
            appGridPane.add(formDetailedView, 1, 0);
            break;
        case Table:
            formTableView.setVisible(true);
            appGridPane.add(formTableView, 1, 0);
            formDetailedController.onNavigatedAway();
            break;
    }
}

    public GridPane getAppGridPane() {
    return appGridPane;
}
    public Node getDetailedViewNode() {
        return formDetailedView;
    }



}





 
