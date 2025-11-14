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
        ObservableList<TCEPForm> forms = FXCollections.observableArrayList();
        // this query ONLY uses columns we know exist right now
        String sql =
            "SELECT f.FormID, f.RequestDate, f.Term, f.Year, " +
            "       f.StudentID, f.StatusID, f.NetID, s.Student_Name " +
            "FROM TCEP_Form f " +
            "JOIN Student s ON s.StudentID = f.StudentID " +
            "ORDER BY f.RequestDate DESC";

        try {
            Connection conn = TCEPDatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return forms;
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
