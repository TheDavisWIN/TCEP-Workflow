/***********************************************************************************************************************
 * Service that provides connection to the MySQL database
***********************************************************************************************************************/

package utd.tcep.db;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import utd.tcep.data.FormHistoryEntry;

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


 /**
 * Returns full history for a specific form with readable status names and advisor name.
 * Used by TCEPHistoryController to populate the history table.
 * Andrew Robertson (AMR220023)
 */

public static ObservableList<FormHistoryEntry> getFormHistory(int formId) {
    ObservableList<FormHistoryEntry> history = FXCollections.observableArrayList();
    
    String sql = "SELECT " +
                 "    h.Changed_On as date, " +
                 "    ts.StatusName as action, " +
                 "    COALESCE(a.Advisor_Name, 'System') as reviewer, " +
                 "    h.Comments " +
                 "FROM tcep_status_history h " +
                 "JOIN transfer_status ts ON h.StatusID = ts.StatusID " +
                 "LEFT JOIN advisor a ON h.AdvisorID = a.AdvisorID " +
                 "WHERE h.FormID = ? " +
                 "ORDER BY h.Changed_On DESC";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
         
        ps.setInt(1, formId);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            String dateStr = rs.getTimestamp("date")
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
            history.add(new FormHistoryEntry(
                dateStr,
                rs.getString("action"),
                rs.getString("reviewer")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return history;
}



/**
 * Logs a status change in tcep_status_history and updates the current StatusID in tcep_form.
 * Called whenever advisor clicks Approve / Deny / Send Back.
 * Ensures both the main table and history view stay in sync.
 * Andrew Robertson (AMR220023)
 */
public static void logStatusChange(int formId, int newStatusId, String comments, Integer advisorId) {
    String historySql = "INSERT INTO tcep_status_history (FormID, StatusID, Changed_On, Comments, AdvisorID) " +
                        "VALUES (?, ?, NOW(), ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement historyPs = conn.prepareStatement(historySql);
         PreparedStatement updatePs = conn.prepareStatement(
             "UPDATE tcep_form SET StatusID = ? WHERE FormID = ?")) {

        // First: Insert into history
        historyPs.setInt(1, formId);
        historyPs.setInt(2, newStatusId);
        historyPs.setString(3, comments == null || comments.trim().isEmpty() ? null : comments.trim());
        if (advisorId == null) {
            historyPs.setNull(4, java.sql.Types.INTEGER);
        } else {
            historyPs.setInt(4, advisorId);
        }
        historyPs.executeUpdate();

        // Second: Update the form's current status
        updatePs.setInt(1, newStatusId);
        updatePs.setInt(2, formId);
        updatePs.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
        // Optional: show alert to user
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to update form status.");
            alert.show();
        });
    }
}

private static void updateFormStatus(int formId, int newStatusId) {
    String sql = "UPDATE tcep_form SET StatusID = ? WHERE FormID = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, newStatusId);
        ps.setInt(2, formId);
        ps.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}
