/***********************************************************************************************************************
 * Service that provides connection to the MySQL database
***********************************************************************************************************************/

package utd.tcep.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
