/***********************************************************************************************************************
 * Service that provides connection to the MySQL database
***********************************************************************************************************************/

package utd.tcep.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.data.TCEPForm;
import utd.tcep.data.TCEPFormTable;


public class TCEPDatabaseService {

    // connection info for local XAMPP MySQL instance.
    private static final String URL  = "jdbc:mysql://localhost:3306/tcep";
    private static final String USER = "root";      // change if needed
    private static final String PASS = "";          // change if you set a password
    private static Connection connection = null;

    // load the MySQL driver once when the class is first used.
    // This makes sure DriverManager knows about com.mysql.cj.jdbc.Driver.
    // written by Jeffrey Chou (jxc033200)
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
    }

    /**
     * Returns a live JDBC connection to the local 'tcep' database.
     * @return Connection to MySQL.
     * @throws SQLException if the database is not reachable (service down, wrong port, wrong DB name, etc.)
     * written by Jeffrey Chou (jxc033200)
     
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }*/

    //updated here by Ayden Benel
    //one single connection for the DB, if there is no connection a new connection will be made
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {

            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Database connected");
        }
        return connection;
        
    }

    //when the app is closed so does the connection
    public static void closeConnection() {
        try {

            if (connection != null && !connection.isClosed()) {
 
                connection.close();
                System.out.println("DB closed");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Written by Jeffrey Chou (jxc033200) and Ryan Pham (rkp200003)
    public static ObservableList<TCEPForm> getFormsFromDB() {
        return getFormsFromDB(null);
    }

    /**
     * Get forms from database, optionally filtered by advisor ID.
     * If advisorId is null, returns all forms.
     * If advisorId is provided, returns only forms where:
     *   1. Student is assigned to this advisor (Student.AdvisorID = advisorId), OR
     *   2. Form has been sent to this advisor (tcep_form_status_history.AssignedAdvisorID = advisorId)
     */
    public static ObservableList<TCEPForm> getFormsFromDB(Integer advisorId) {
        ObservableList<TCEPForm> forms = FXCollections.observableArrayList();
        
        String sql;
        if (advisorId == null) {
            // Return all forms (admin view)
            sql = "SELECT f.FormID, f.RequestDate, f.Term, f.Year, " +
                  "       f.StudentID, f.StatusID, f.NetID, s.Student_Name " +
                  "FROM TCEP_Form f " +
                  "JOIN Student s ON s.StudentID = f.StudentID " +
                  "ORDER BY f.RequestDate DESC";
        } else {
            // Return forms assigned to or sent to this advisor
            sql = "SELECT DISTINCT f.FormID, f.RequestDate, f.Term, f.Year, " +
                  "       f.StudentID, f.StatusID, f.NetID, s.Student_Name " +
                  "FROM TCEP_Form f " +
                  "JOIN Student s ON s.StudentID = f.StudentID " +
                  "LEFT JOIN tcep_form_status_history h ON h.FormID = f.FormID " +
                  "WHERE s.AdvisorID = ? OR h.AssignedAdvisorID = ? " +
                  "ORDER BY f.RequestDate DESC";
        }

        try {
            Connection conn = TCEPDatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            
            if (advisorId != null) {
                System.out.println("Loading forms for advisorId: " + advisorId);
                ps.setInt(1, advisorId);
                ps.setInt(2, advisorId);
            } else {
                System.out.println("Loading all forms (no advisor filter)");
            }
            
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                TCEPForm f = new TCEPForm(rs.getInt("FormID")); 
                f.setStudentName(rs.getString("Student_Name"));    
                f.setUtdId(String.valueOf(rs.getInt("StudentID")));    
                f.setNetId(rs.getString("NetID"));
                java.sql.Date d = rs.getDate("RequestDate");
                if (d != null) {
                    f.setStartedDate(d.toLocalDate());
                }
                f.setStatus(String.valueOf(rs.getInt("StatusID")));
                forms.add(f);
                count++;
            }
            System.out.println("Loaded " + count + " forms from database");
        } catch (SQLException e) {
            System.err.println("Error loading forms: " + e.getMessage());
            e.printStackTrace();
        }

        return forms;
    }

    /**
     * Insert a status/history row for a form action.
     * If advisorId is non-null, the row will be associated with that advisor.
     * If departmentName is non-null, it will be stored in the DepartmentName column.
     */
    public static void addStatusChange(int formId, String actionType, String comments, Integer advisorId, String departmentName) {
        String sql = "INSERT INTO tcep_form_status_history (FormID, ActionType, Comments, AssignedAdvisorID, DepartmentName, ActionDate) VALUES (?, ?, ?, ?, ?, NOW())";
        try {
            Connection conn = TCEPDatabaseService.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, formId);
                ps.setString(2, actionType);
                ps.setString(3, comments);
                if (advisorId != null) ps.setInt(4, advisorId); else ps.setNull(4, java.sql.Types.INTEGER);
                if (departmentName != null) ps.setString(5, departmentName); else ps.setNull(5, java.sql.Types.VARCHAR);
                int rows = ps.executeUpdate();
                System.out.println("Added status change: FormID=" + formId + ", Action=" + actionType + ", AdvisorID=" + advisorId + ", Dept=" + departmentName + ", Rows affected=" + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error adding status change: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the status of a form in the tcep_form table.
     */
    public static void updateFormStatus(int formId, int newStatusId) {
        String sql = "UPDATE tcep_form SET StatusID = ? WHERE FormID = ?";
        try {
            Connection conn = TCEPDatabaseService.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newStatusId);
                ps.setInt(2, formId);
                int rows = ps.executeUpdate();
                System.out.println("Updated form status: FormID=" + formId + ", NewStatusID=" + newStatusId + ", Rows affected=" + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error updating form status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveForms(TCEPFormTable formTable) {
        try {
            Connection conn = TCEPDatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
