/***********************************************************************************************************************
 * Data object that holds TCEP forms and connects with the database service to populate the table
***********************************************************************************************************************/

package utd.tcep.data;

import java.sql.SQLException;
import java.time.LocalDate;
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
     * @param advisorId Optional advisor ID to filter forms (null for all forms)
     * Written by Jeffrey Chou (jxc033200)
     * Modified by Nicolas Hartono (nxh210004) to filter by advisor
     */
    public void loadForms(Integer advisorId) throws SQLException {
        rows.clear();
        
        // Get data from database layer
        try (ResultSet rs = TCEPDatabaseService.getAllForms(advisorId)) {
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

    /**
     * Creates a new blank TCEPForm with a unique FormID and current date.
     * Adds the new form to the rows list.
     * @return The newly created blank TCEPForm
     * Written by Jeffrey Chou (jxc033200)
     */
    public TCEPForm createBlankForm() {
        TCEPForm newForm = new TCEPForm(getNewFormID());
        newForm.setStartedDate(LocalDate.now());
        rows.add(newForm);
        return newForm;
    }

    /**
     * Generates a new unique FormID by finding the maximum existing FormID and adding 1.
     * Queries the database to ensure the ID is truly unique.
     * @return A new unique FormID
     * Written by Jeffrey Chou (jxc033200)
     * Modified by Nicolas Hartono (nxh210004) to query database
     */
    public int getNewFormID() {
        int maxID = 0;
        
        // Check in-memory rows first
        for (TCEPForm form : rows) {
            if (form.formID > maxID) {
                maxID = form.formID;
            }
        }
        
        // Also check database to ensure we don't have ID conflicts
        try {
            Integer dbMaxId = TCEPDatabaseService.getMaxFormID();
            if (dbMaxId != null && dbMaxId > maxID) {
                maxID = dbMaxId;
            }
        } catch (SQLException e) {
            System.err.println("Error getting max FormID from database: " + e.getMessage());
        }
        
        return maxID + 1;
    }
}