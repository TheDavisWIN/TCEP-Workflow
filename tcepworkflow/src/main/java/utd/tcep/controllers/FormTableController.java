/***********************************************************************************************************************
 * JavaFX controller that handles the view of the table of forms
 * Allows opening of forms and quick actions from the table view
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import utd.tcep.data.TCEPForm;
import utd.tcep.db.TCEPDatabaseService;

public class FormTableController {

// from FXML
    @FXML private TableView<TCEPForm> formTable;
    @FXML private TableColumn<TCEPForm, String> studentNameCol;
    @FXML private TableColumn<TCEPForm, String> utdIdCol;
    @FXML private TableColumn<TCEPForm, String> netIdCol;
    @FXML private TableColumn<TCEPForm, LocalDate> dateStartedCol;
    @FXML private TableColumn<TCEPForm, String> statusCol;
    @FXML private Label dbStatus;   // "DB: not tested yet"

    @FXML
    public void initialize() {
        // 1. bind columns to TCEPForm getters
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        utdIdCol.setCellValueFactory(new PropertyValueFactory<>("utdId"));
        netIdCol.setCellValueFactory(new PropertyValueFactory<>("netId"));
        dateStartedCol.setCellValueFactory(new PropertyValueFactory<>("startedDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. load data from DB
        loadForms();
    }

    private void loadForms() {
        ObservableList<TCEPForm> rows = FXCollections.observableArrayList();

        // this query ONLY uses columns we know exist right now
        String sql =
            "SELECT FormID, RequestDate, Term, Year, StudentID, StatusID " +
            "FROM TCEP_Form";

        try (Connection conn = TCEPDatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TCEPForm f = new TCEPForm();

                int studentId = rs.getInt("StudentID");

                // we don't have name / netid in this table yet, so fake them for now
                f.setStudentName("Student " + studentId);
                f.setUtdId(String.valueOf(studentId));
                f.setNetId("net" + studentId);

                // date
                java.sql.Date d = rs.getDate("RequestDate");
                if (d != null) {
                    f.setStartedDate(d.toLocalDate());
                }

                // status – db only has StatusID, so show it
                int statusId = rs.getInt("StatusID");
                f.setStatus("Status " + statusId);

                rows.add(f);
            }

            formTable.setItems(rows);

            if (dbStatus != null) {
                dbStatus.setText("DB: ✅ loaded " + rows.size() + " form(s)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (dbStatus != null) {
                dbStatus.setText("DB: ❌ " + e.getMessage());
            }
        }
    }

    // your test button in the UI can still call this
    @FXML
    private void onRefreshClicked() {
        loadForms();
    }
    
}
 