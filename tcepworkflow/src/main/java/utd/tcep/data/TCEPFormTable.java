/***********************************************************************************************************************
 * Data object that holds TCEP forms and connects with the database service to populate the table
***********************************************************************************************************************/

package utd.tcep.data;

import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.db.TCEPDatabaseService;

/**
 * Business Logic Layer - TCEPFormTable
 * Transforms database results into domain objects (TCEPForm)
 * Acts as a bridge between the database layer and presentation layer
 * Written by Jeffrey Chou (jxc033200)
 */
public class TCEPFormTable {
    public ObservableList<TCEPForm> rows = FXCollections.observableArrayList();

    /**
     * Retrieves form records from the database service and populates the TableView.
     * Calls the database layer to get data, then transforms ResultSet into TCEPForm objects.
     * Each row from the ResultSet is converted into a TCEPForm object and added to an ObservableList,
     * which is then bound to the TableView for display.
     * Written by Jeffrey Chou (jxc033200)
     */
    public void loadForms() throws SQLException {
        rows.clear();
        
        // Get data from database layer
        try (ResultSet rs = TCEPDatabaseService.getAllForms()) {
            // Transform ResultSet into domain objects
            while (rs.next()) {
                TCEPForm f = new TCEPForm(rs.getInt("FormID"));
                
                // Set student information
                f.setStudentName(rs.getString("Student_Name"));    
                if (rs.getObject("UtdID") != null) {
                    f.setUtdId(String.valueOf(rs.getInt("UtdID")));
                } else {
                    f.setUtdId(null);
                }
                if (rs.getObject("NetID") != null) {
                    f.setNetId(rs.getString("NetID"));
                } else {
                    f.setNetId(null);
                }
                
                // Set institution information
                f.setSchoolName(rs.getString("Institution_Name"));
                
                // Set date
                java.sql.Date d = rs.getDate("RequestDate");
                if (d != null) {
                    f.setStartedDate(d.toLocalDate());
                }
                
                // Set status
                f.setStatus(String.valueOf(rs.getInt("StatusID")));
                
                rows.add(f);
            }
        }
    }

    public TCEPForm createBlankForm() {
        TCEPForm newForm = new TCEPForm(getNewFormID());
        newForm.setStartedDate(LocalDate.now());
        rows.add(newForm);
        return newForm;
    }

    public int getNewFormID() {
        int maxID = 0;
        for (TCEPForm form : rows) {
            if (form.formID > maxID) {
                maxID = form.formID;
            }
        }
        return maxID + 1;
    }
}