// Controller file to handle view history button and back to form button on formdetailedview.fxml 
//Andrew Robertson (AMR220023)

package utd.tcep.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import utd.tcep.data.FormHistoryEntry;
import utd.tcep.db.TCEPDatabaseService;

public class TCEPHistoryController {

    @FXML private Button backToFormButton;
    @FXML private TableView<FormHistoryEntry> historyTable;
    @FXML private TableColumn<FormHistoryEntry, String> dateColumn;
    @FXML private TableColumn<FormHistoryEntry, String> actionColumn;
    @FXML private TableColumn<FormHistoryEntry, String> reviewerColumn;

    private NavigationController navigationController;
    private int currentFormId = -1;  // Will be set from FormDetailedController

    public void setNavigationController(NavigationController nav) {
        this.navigationController = nav;

        if (backToFormButton != null) {
            backToFormButton.setOnAction(e ->
                navigationController.swapView(NavigationController.View.Detailed)
            );
        }
    }

    // NEW: Called from FormDetailedController when user clicks "View History"
    public void loadHistoryForForm(int formId) {
        this.currentFormId = formId;
        refreshHistory();
    }

    private void refreshHistory() {
        if (currentFormId <= 0) {
            historyTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<FormHistoryEntry> data = TCEPDatabaseService.getFormHistory(currentFormId);
        historyTable.setItems(data);
    }

    @FXML
    public void initialize() {
        // Set up cell value factories
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        reviewerColumn.setCellValueFactory(new PropertyValueFactory<>("reviewer"));

        // Make columns resize nicely
        dateColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.33));
        actionColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.34));
        reviewerColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.33));

        // Optional: show placeholder if empty
        historyTable.setPlaceholder(new javafx.scene.control.Label("No history available for this form."));
    }
}