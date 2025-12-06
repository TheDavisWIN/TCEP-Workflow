package utd.tcep.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import utd.tcep.data.TCEPStatusHistory;
import utd.tcep.db.TCEPDatabaseService;
import javafx.scene.control.TableCell;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TCEPHistoryController {

    @FXML private Button backToFormButton;
    @FXML private TableView<TCEPStatusHistory> historyTable;
    @FXML private TableColumn<TCEPStatusHistory, LocalDateTime> dateColumn;
    @FXML private TableColumn<TCEPStatusHistory, String> actionColumn;
    @FXML private TableColumn<TCEPStatusHistory, String> reviewerColumn;

    private FormDetailedController parentController;
    private int currentFormId;


    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("changedOn"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        reviewerColumn.setCellValueFactory(new PropertyValueFactory<>("advisorName"));

        // Fixed cell factory – works on every Java version
        dateColumn.setCellFactory(column -> new TableCell<TCEPStatusHistory, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
    }

    // Called from FormDetailedController to pass data
    public void setFormData(int formId, FormDetailedController parent) {
        this.currentFormId = formId;
        this.parentController = parent;
        loadHistory();
    }

    private void loadHistory() {
        ObservableList<TCEPStatusHistory> historyList = FXCollections.observableArrayList();

    String sql = "SELECT h.Changed_On, h.Comments, a.Advisor_Name " +
             "FROM TCEP_Status_History h " +
             "LEFT JOIN Advisor a ON h.AdvisorID = a.AdvisorID " +
             "WHERE h.FormID = ? " +
             "ORDER BY h.Changed_On DESC";

        try (var conn = TCEPDatabaseService.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentFormId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TCEPStatusHistory entry = new TCEPStatusHistory(
                    rs.getTimestamp("Changed_On").toLocalDateTime(),
                    rs.getString("Comments"),
                    rs.getString("Advisor_Name")
                );
                historyList.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // You can show an alert here later
        }

        historyTable.setItems(historyList);
    }

    @FXML
    private void handleBackToForm() {
        if (parentController == null) return;

        GridPane grid = parentController.getNavigationController().getAppGridPane();
        
        // Remove the history view if it exists
        Node historyView = parentController.getCurrentHistoryView();
        if (historyView != null) {
            grid.getChildren().remove(historyView);
            parentController.clearCurrentHistoryView();
        }

        // Ensure column 1 is clean and re-add the original detailed form
        grid.getChildren().removeIf(n -> GridPane.getColumnIndex(n) != null && GridPane.getColumnIndex(n) == 1);
        Node detailedView = parentController.getNavigationController().getDetailedViewNode();
        if (detailedView != null) {
            grid.add(detailedView, 1, 0);
        }
    }

    // Helper to allow parent to access navigation (we'll add a getter in FormDetailedController)
    // This avoids circular dependency
}