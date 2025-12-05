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

    //updated here by Ayden Benel (acb210001)
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


//Check if everything is in the databse or not, written by Ayden Benel (acb210001)
//does institution exists
public static String checkInstitution(String name) {
    String sql = "SELECT InstitutionID FROM institution WHERE Institution_Name = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return "✔ Institution exists (ID = " + rs.getInt("InstitutionID") + ")";
        } else {
            return "✘ Institution does NOT exist in database.";

        }
    } catch (SQLException e) {
        return "⚠ SQL Error (institution check): " + e.getMessage();
    }
}


//equivalent UTD course exists
public static String checkEquivalentCourse(String utdCourseNumber) {
    String sql = "SELECT Equivalent_CourseID FROM equivalent_course WHERE UTDCourseNumber = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, utdCourseNumber);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return "✔ Equivalent UTD course exists (Equivalent_CourseID = " + rs.getInt("Equivalent_CourseID") + ")";
        } else {
            return "✘ Equivalent UTD course NOT found (UTDCourseNumber = " + utdCourseNumber + ")";
        }
    } catch (SQLException e) {
        return "⚠ SQL Error (equivalent course check): " + e.getMessage();
    }
}


// Check if incoming transfer course exists
public static String checkIncomingCourse(String institutionName, String courseNumber) {
    String sql = "SELECT Incoming_CourseID FROM incoming_course "
               + "WHERE Institution_Name = ? AND CourseNumber = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, institutionName);
        ps.setString(2, courseNumber);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return "✔ Incoming transfer course exists (Incoming_CourseID = " + rs.getInt("Incoming_CourseID") + ")";
        } else {
            return "✘ Incoming transfer course NOT found (" + institutionName + ", " + courseNumber + ")";
        }
    } catch (SQLException e) {
        return "⚠ SQL Error (incoming course check): " + e.getMessage();
    }
}


//mapping exists inside course_equivalency but since incoming_course already has its equivalnet UTD course in it its not needed, I originally added this to test how it would work but its not needed right now, it could be implemented later
public static String checkCourseEquivalency(String institutionName, String courseNumber, String utdCourseNumber) {

    String sql =
        "SELECT ce.Incoming_CourseID, ce.Equivalent_CourseID" +
        " FROM course_equivalency ce" +
        " JOIN incoming_course ic ON ic.Incoming_CourseID = ce.Incoming_CourseID" +
        " JOIN equivalent_course ec ON ec.Equivalent_CourseID = ce.Equivalent_CourseID" +
        " WHERE ic.Institution_Name = ? " +
        "   AND ic.CourseNumber = ? " +
        "   AND ec.UTDCourseNumber = ?;";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, institutionName);
        ps.setString(2, courseNumber);
        ps.setString(3, utdCourseNumber);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return "✔ Equivalency EXISTS (Incoming_CourseID = " +
                   rs.getInt("Incoming_CourseID") +
                   ", Equivalent_CourseID = " +
                   rs.getInt("Equivalent_CourseID") + ")";
        } else {
            return "✘ No equivalency mapping found between incoming course and UTD course";
        }

    } catch (SQLException e) {
        return "⚠ SQL Error (equivalency check): " + e.getMessage();
    }
}


//runs all checks and returns the report
public static String verifyEquivalency(String institutionName, String incomingCourseNumber, String utdCourseNumber) {

    StringBuilder report = new StringBuilder();

    //Institution check
    report.append(checkInstitution(institutionName)).append("\n");

    //Equivalent UTD course check
    report.append(checkEquivalentCourse(utdCourseNumber)).append("\n");

    //Incoming course check
    report.append(checkIncomingCourse(institutionName, incomingCourseNumber)).append("\n");

    //Final mapping check left out since the other three confirm it already
    //report.append(checkCourseEquivalency(institutionName, incomingCourseNumber, utdCourseNumber)).append("\n");


    return report.toString();
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
