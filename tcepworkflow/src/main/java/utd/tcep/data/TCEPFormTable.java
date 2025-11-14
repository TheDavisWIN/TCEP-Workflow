/***********************************************************************************************************************
 * Data object that holds TCEP forms and connects with the database service to populate the table
***********************************************************************************************************************/

package utd.tcep.data;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.db.TCEPDatabaseService;

public class TCEPFormTable {
    public ObservableList<TCEPForm> rows = FXCollections.observableArrayList();

    /**
     * Retrieves form records from the MySQL database and populates the TableView.
     * Executes a SQL SELECT query on the TCEP_Form table (and related tables in the future).
     * Each row from the ResultSet is converted into a TCEPForm object and added to an ObservableList,
     * which is then bound to the TableView for display.
     * written by Jeffrey Chou (jxc033200)
     */
    public void loadForms() throws SQLException {
        rows.clear();
        // this query ONLY uses columns we know exist right now
        String sql =
            "SELECT f.FormID, f.RequestDate, f.Term, f.Year, s.UtdID, s.NetID, " +
            "       f.StudentID, f.StatusID, f.NetID, s.Student_Name, i.Institution_Name " +
            "FROM TCEP_Form f " +
            "JOIN Student s ON s.StudentID = f.StudentID " +
            "JOIN Institution i ON i.InstitutionID = f.InstitutionID " +
            "ORDER BY f.RequestDate DESC";

        Connection conn = TCEPDatabaseService.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TCEPForm f = new TCEPForm(); 
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
                f.setSchoolName(rs.getString("Institution_Name"));
                java.sql.Date d = rs.getDate("RequestDate");
                if (d != null) {
                    f.setStartedDate(d.toLocalDate());
                }
                f.setStatus(String.valueOf(rs.getInt("StatusID")));
                rows.add(f);
            }
        }
    }

    public TCEPForm createBlankForm() {
        TCEPForm newForm = new TCEPForm();
        rows.add(newForm);
        return newForm;
    }
}