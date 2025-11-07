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
            "SELECT FormID, RequestDate, Term, Year, StudentID, StatusID " +
            "FROM TCEP_Form";

        Connection conn = TCEPDatabaseService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            TCEPForm f = new TCEPForm();

            int studentId = rs.getInt("StudentID");

            // **Student NAME and NETID are placeholders** - (add attributes to TCEP Form? or JOIN)
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
    }

    public TCEPForm createBlankForm() {
        TCEPForm newForm = new TCEPForm();
        rows.add(newForm);
        return newForm;
    }
}