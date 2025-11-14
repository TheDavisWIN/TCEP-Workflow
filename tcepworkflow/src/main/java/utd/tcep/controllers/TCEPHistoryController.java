// Controller file to handle view history button and back to form button on formdetailedview.fxml 
//Andrew Robertson (AMR220023)



package utd.tcep.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class TCEPHistoryController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button backToFormButton;

    @FXML
    private Button formsButton;

    @FXML
    private Button startEmptyFormButton;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<?> historyTable;

    @FXML
    private TableColumn<?, ?> dateColumn; 
    @FXML
    private TableColumn<?, ?> actionColumn;
    @FXML
    private TableColumn<?, ?> reviewerColumn;

    private NavigationController navigationController;

    public void setNavigationController(NavigationController nav) {
    this.navigationController = nav;

    if (formsButton != null) {
    formsButton.setOnAction(e ->
        navigationController.swapView(NavigationController.View.Table)
    );
}

if (startEmptyFormButton != null) {
    startEmptyFormButton.setOnAction(e ->
        navigationController.swapView(NavigationController.View.Detailed)
    );
}

if (logoutButton != null) {
    logoutButton.setOnAction(e ->
        System.out.println("Logging out...") 
    );
}

    if (backToFormButton != null) {
        backToFormButton.setOnAction(e ->
            navigationController.swapView(NavigationController.View.Detailed)
        );
    }
}

    @FXML
    private void handleBackToForm() {
        navigationController.swapView(NavigationController.View.Detailed);
    }

    @FXML
    public void initialize() {
        // Bind table columns to fill the width evenly
        dateColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.33));
        actionColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.34));
        reviewerColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.33));

}
}
