/***********************************************************************************************************************
 * Handles navigation bar
 * Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import utd.tcep.data.TCEPForm;
import utd.tcep.events.NavigationRequestEvent;
import utd.tcep.main.TCEPWorkflowApp;

public class NavigationController {

    public enum View {
        Detailed,
        Table
    }

    @FXML
    private GridPane appGridPane;
    private Node formDetailedView;
    private Node formTableView;
    private FormDetailedController formDetailedController;
    private FormTableController formTableController;

    // Automatically called on program start, saving controllers for future method calls
    // Ryan Pham (rkp200003)
    @FXML
    public void initialize() throws IOException {
        FXMLLoader formDetailedViewLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdetailedview.fxml"));
        FXMLLoader formTableViewLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formtableview.fxml"));
        formDetailedView = formDetailedViewLoader.load();
        formTableView = formTableViewLoader.load();
        appGridPane.getChildren().add(formDetailedView);
        appGridPane.getChildren().add(formTableView);
        GridPane.setColumnIndex(formDetailedView, 1);
        GridPane.setColumnIndex(formTableView, 1);
        formDetailedController = formDetailedViewLoader.getController();
        formTableController = formTableViewLoader.getController();

        formTableController.formTable.addEventHandler(NavigationRequestEvent.REQUEST, event -> {
            swapView(View.Detailed);
            formDetailedController.setForm(event.getForm());
            System.out.println("worked");
        });
    }

    // Show the full form table
    // Ryan Pham (rkp200003)
    @FXML
    private void handleShowFormTable() throws IOException {
        swapView(View.Table);
    }

    // Create a new form with blank fields
    // Ryan Pham (rkp200003)
    @FXML
    private void handleShowBlankForm() throws IOException {
        swapView(View.Detailed);
        formDetailedController.setForm(formTableController.getFormTableObject().createBlankForm());
        System.out.println(formTableController.getFormTableObject().rows.size());
    }

    // Ryan Pham (rkp200003)
    @FXML
    private void handleLogout() throws IOException {
        System.out.println("Logout");
    }

    // Swap between different views and load FXML when navigation buttons are clicked
    // Ryan Pham (rkp200003)
    public void swapView(View view) {
        switch (view) {
            case Detailed:
                formDetailedView.setVisible(true);
                formTableView.setVisible(false);
                break;
            case Table:
                formDetailedView.setVisible(false);
                formTableView.setVisible(true);
                break;
        }
    }
}
 