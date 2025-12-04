/***********************************************************************************************************************
 * Service that provides connection to the MySQL database
***********************************************************************************************************************/

package utd.tcep.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

    // ==================== DATA ACCESS METHODS (DATABASE LAYER) ====================
    
    /**
     * Gets all forms from the database
     * @return ResultSet containing all form data
     * @throws SQLException if database error occurs
     */
    public static ResultSet getAllForms() throws SQLException {
        String sql =
            "SELECT f.FormID, f.RequestDate, f.Term, f.Year, s.UtdID, s.NetID, " +
            "       f.StudentID, f.StatusID, f.NetID, s.Student_Name, i.Institution_Name " +
            "FROM TCEP_Form f " +
            "JOIN Student s ON s.StudentID = f.StudentID " +
            "JOIN Institution i ON i.InstitutionID = f.InstitutionID " +
            "ORDER BY f.RequestDate DESC";

        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }
    
    /**
     * Checks if a user exists in the advisor table
     * @param email The advisor email
     * @return true if user exists, false otherwise
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static boolean userExists(String email) throws SQLException {
        String sql = "SELECT * FROM advisor WHERE Advisor_Email = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Gets advisor information by email
     * @param email The advisor email
     * @return Map containing AdvisorID and Advisor_Name, or null if not found
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static Map<String, Object> getAdvisorByEmail(String email) throws SQLException {
        String sql = "SELECT AdvisorID, Advisor_Name FROM advisor WHERE Advisor_Email = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> advisor = new HashMap<>();
                    advisor.put("AdvisorID", rs.getInt("AdvisorID"));
                    advisor.put("Advisor_Name", rs.getString("Advisor_Name"));
                    return advisor;
                }
            }
        }
        return null;
    }
    
    /**
     * Retrieves form data by student ID (UtdID or NetID)
     * @param id Student UtdID or NetID
     * @return Map containing all form field data, or null if not found
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static Map<String, Object> getFormDataById(String id) throws SQLException {
        String sql = "SELECT f.FormID, f.StudentID, s.Student_Name, f.Degree_Requirement, f.Core_Designation, "
                + "s.UtdID, s.NetID, f.Incoming_CourseID, ic.CourseName AS IncomingCourseName, ic.CourseNumber AS IncomingCourseNumber, "
                + "f.Equivalent_CourseID, ec.UTDCourseNumber AS EquivalentCourseNumber, "
                + "f.InstitutionID, inst.Institution_Name AS InstitutionName, "
                + "f.StartAdvisorID, adv.Advisor_Name AS StartAdvisorName, f.RequestDate "
                + "FROM tcep_form f "
                + "LEFT JOIN student s ON f.StudentID = s.StudentID "
                + "LEFT JOIN incoming_course ic ON f.Incoming_CourseID = ic.Incoming_CourseID "
                + "LEFT JOIN equivalent_course ec ON f.Equivalent_CourseID = ec.Equivalent_CourseID "
                + "LEFT JOIN institution inst ON f.InstitutionID = inst.InstitutionID "
                + "LEFT JOIN advisor adv ON f.StartAdvisorID = adv.AdvisorID "
                + "WHERE s.UtdID = ? OR s.NetID = ?";

        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> formData = new HashMap<>();
                    formData.put("FormID", rs.getInt("FormID"));
                    formData.put("StudentName", rs.getString("Student_Name"));
                    formData.put("UtdID", rs.getObject("UtdID"));
                    formData.put("NetID", rs.getString("NetID"));
                    formData.put("IncomingCourseNumber", rs.getString("IncomingCourseNumber"));
                    formData.put("IncomingCourseName", rs.getString("IncomingCourseName"));
                    formData.put("InstitutionName", rs.getString("InstitutionName"));
                    formData.put("EquivalentCourseNumber", rs.getString("EquivalentCourseNumber"));
                    formData.put("DegreeRequirement", rs.getString("Degree_Requirement"));
                    formData.put("CoreDesignation", rs.getString("Core_Designation"));
                    formData.put("StartAdvisorName", rs.getString("StartAdvisorName"));
                    formData.put("RequestDate", rs.getDate("RequestDate"));
                    return formData;
                }
            }
        }
        return null;
    }

    /**
     * Gets the status ID for a given status name, creates it if it doesn't exist
     * @param statusName The name of the status (e.g., "Denied", "Sent Back", "Approved")
     * @param categoryId The category ID (1=Pending, 2=Approved, 3=Denied, 4=Sent Back)
     * @return The status ID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static int getOrCreateStatusId(String statusName, int categoryId) throws SQLException {
        Connection conn = getConnection();
        
        // First, ensure the category exists
        ensureCategoryExists(categoryId, statusName);
        
        // Check if status exists
        String checkSql = "SELECT StatusID FROM transfer_status WHERE StatusName = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, statusName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("StatusID");
                }
            }
        }
        
        // Status doesn't exist, create it
        String insertSql = "INSERT INTO transfer_status (StatusName, CategoryID) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, statusName);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        throw new SQLException("Failed to get or create status: " + statusName);
    }

    /**
     * Ensures a status category exists in the database
     * @param categoryId The category ID
     * @param categoryName The category name
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    private static void ensureCategoryExists(int categoryId, String categoryName) throws SQLException {
        Connection conn = getConnection();
        
        // Check if category exists
        String checkSql = "SELECT CategoryID FROM status_category WHERE CategoryID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return; // Category exists
                }
            }
        }
        
        // Category doesn't exist, create it
        String insertSql = "INSERT INTO status_category (CategoryID, CategoryName) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, categoryName);
            ps.executeUpdate();
        }
    }

    /**
     * Adds a status history entry with a reason (for deny/send back actions)
     * @param formId The form ID
     * @param statusId The status ID (from Transfer_Status table)
     * @param reason The reason/comments for the status change
     * @param studentId The student ID (optional)
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static void addStatusHistory(int formId, int statusId, String reason, Integer studentId) throws SQLException {
        String sql = "INSERT INTO TCEP_Status_History (Changed_On, Comments, FormID, StatusID, StudentID) " +
                     "VALUES (NOW(), ?, ?, ?, ?)";
        
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, formId);
            ps.setInt(3, statusId);
            if (studentId != null) {
                ps.setInt(4, studentId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Updates the status of a form
     * @param formId The form ID
     * @param statusId The new status ID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static void updateFormStatus(int formId, int statusId) throws SQLException {
        String sql = "UPDATE tcep_form SET StatusID = ? WHERE FormID = ?";
        
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setInt(2, formId);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a form from the database
     * @param formId The form ID to delete
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static void deleteForm(int formId) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Delete status history first (foreign key constraint)
            String deleteHistorySql = "DELETE FROM TCEP_Status_History WHERE FormID = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteHistorySql)) {
                ps.setInt(1, formId);
                ps.executeUpdate();
            }
            
            // Delete the form
            String deleteFormSql = "DELETE FROM tcep_form WHERE FormID = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteFormSql)) {
                ps.setInt(1, formId);
                int rowsDeleted = ps.executeUpdate();
                if (rowsDeleted == 0) {
                    throw new SQLException("Form not found: " + formId);
                }
            }
            
            conn.commit();
            System.out.println("Form " + formId + " deleted successfully");
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Gets or creates a student record
     * @param studentName Student's full name
     * @param utdId UTD ID (can be null)
     * @param netId Net ID (can be null)
     * @param advisorId Advisor ID (can be null)
     * @return The StudentID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static int getOrCreateStudent(String studentName, String utdId, String netId, Integer advisorId) throws SQLException {
        Connection conn = getConnection();
        
        // Try to find existing student by NetID or UtdID
        String selectSql = "SELECT StudentID FROM student WHERE (NetID = ? AND NetID IS NOT NULL) OR (UtdID = ? AND UtdID IS NOT NULL)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, netId);
            ps.setString(2, utdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int studentId = rs.getInt("StudentID");
                    
                    // Update the existing student record with new data
                    String updateSql = "UPDATE student SET Student_Name = ? WHERE StudentID = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, studentName);
                        updatePs.setInt(2, studentId);
                        int rowsUpdated = updatePs.executeUpdate();
                        System.out.println("DEBUG: Updated student name for StudentID=" + studentId + ", rows affected=" + rowsUpdated);
                    }
                    
                    return studentId;
                }
            }
        }
        
        // Student doesn't exist, create new one
        String insertSql = "INSERT INTO student (Student_Name, UtdID, NetID, AdvisorID, Student_Email, DepartmentID) VALUES (?, ?, ?, ?, '', 1)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, studentName);
            ps.setString(2, utdId);
            ps.setString(3, netId);
            if (advisorId != null) {
                ps.setInt(4, advisorId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to create student, no ID obtained");
            }
        }
    }

    /**
     * Gets or creates an incoming course record
     * @param courseName Course name
     * @param courseNumber Course number
     * @param institutionId Institution ID
     * @param departmentId Department ID (use 1 as default if not specified)
     * @param utdCourseNumber UTD course number (must exist in equivalent_course table)
     * @return The Incoming_CourseID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static int getOrCreateIncomingCourse(String courseName, String courseNumber, int institutionId, Integer departmentId, String utdCourseNumber) throws SQLException {
        Connection conn = getConnection();
        
        // Use default department ID of 1 if not provided
        Integer deptId = (departmentId != null) ? departmentId : null;
        
        // Try to find existing course by course number and institution
        String selectSql = "SELECT Incoming_CourseID FROM incoming_course WHERE CourseNumber = ? AND InstitutionID = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, courseNumber);
            ps.setInt(2, institutionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int courseId = rs.getInt("Incoming_CourseID");
                    
                    // Update the existing course record with new data
                    String updateSql = "UPDATE incoming_course SET CourseName = ? WHERE Incoming_CourseID = ?";
                    
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, courseName);
                        updatePs.setInt(2, courseId);
                        int rowsUpdated = updatePs.executeUpdate();
                        System.out.println("DEBUG: Updated incoming course for Incoming_CourseID=" + courseId + ", rows affected=" + rowsUpdated);
                    }
                    
                    return courseId;
                }
            }
        }
        
        // Course doesn't exist, create new one
        // Get institution name for the Institution_Name field
        String instName = "";
        String getInstSql = "SELECT Institution_Name FROM institution WHERE InstitutionID = ?";
        try (PreparedStatement ps = conn.prepareStatement(getInstSql)) {
            ps.setInt(1, institutionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    instName = rs.getString("Institution_Name");
                }
            }
        }
        
        String insertSql = "INSERT INTO incoming_course (CourseName, CourseNumber, InstitutionID, DepartmentID, Institution_Name, UTDCourseNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, courseName);
            ps.setString(2, courseNumber);
            ps.setInt(3, institutionId);
            if (deptId != null) {
                ps.setInt(4, deptId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, instName);
            ps.setString(6, utdCourseNumber);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to create incoming course, no ID obtained");
            }
        }
    }

    /**
     * Gets or creates an institution record
     * @param institutionName Institution name
     * @return The InstitutionID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static int getOrCreateInstitution(String institutionName) throws SQLException {
        Connection conn = getConnection();
        
        // Try to find existing institution by name
        String selectSql = "SELECT InstitutionID FROM institution WHERE Institution_Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, institutionName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("InstitutionID");
                }
            }
        }
        
        // Institution doesn't exist, create new one
        String insertSql = "INSERT INTO institution (Institution_Name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, institutionName);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to create institution, no ID obtained");
            }
        }
    }

    /**
     * Gets or creates an equivalent course record
     * @param courseNumber UTD course number
     * @return The Equivalent_CourseID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     */
    public static int getOrCreateEquivalentCourse(String courseNumber) throws SQLException {
        Connection conn = getConnection();
        
        // Try to find existing equivalent course
        String selectSql = "SELECT Equivalent_CourseID FROM equivalent_course WHERE UTDCourseNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, courseNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Equivalent_CourseID");
                }
            }
        }
        
        // Course doesn't exist, create new one
        String insertSql = "INSERT INTO equivalent_course (UTDCourseNumber) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, courseNumber);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to create equivalent course, no ID obtained");
            }
        }
    }

    /**
     * Creates a new TCEP form with all related records
     * @param formData Map containing all form fields
     * @return The new FormID
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     * 
     * Expected keys in formData Map:
     * - studentName (String)
     * - utdId (String, optional)
     * - netId (String, optional)
     * - advisorId (Integer, optional)
     * - incomingCourseName (String)
     * - incomingCourseNumber (String)
     * - institutionName (String)
     * - equivalentCourseNumber (String)
     * - degreeRequirement (String, optional)
     * - coreDesignation (String, optional)
     * - requestDate (java.sql.Date)
     * - term (String: Fall/Spring/Summer/Other)
     * - year (Integer)
     */
    public static int createForm(Map<String, Object> formData) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Get or create related records
            int studentId = getOrCreateStudent(
                (String) formData.get("studentName"),
                (String) formData.get("utdId"),
                (String) formData.get("netId"),
                (Integer) formData.get("advisorId")
            );
            
            int institutionId = getOrCreateInstitution(
                (String) formData.get("institutionName")
            );
            
            Integer equivalentCourseId = null;
            String equivCourseNum = (String) formData.get("equivalentCourseNumber");
            if (equivCourseNum != null && !equivCourseNum.trim().isEmpty()) {
                equivalentCourseId = getOrCreateEquivalentCourse(equivCourseNum);
            } else {
                // Create a default equivalent course if none provided
                equivCourseNum = "UNKN0000";
                equivalentCourseId = getOrCreateEquivalentCourse(equivCourseNum);
            }
            
            int incomingCourseId = getOrCreateIncomingCourse(
                (String) formData.get("incomingCourseName"),
                (String) formData.get("incomingCourseNumber"),
                institutionId,
                (Integer) formData.get("departmentId"),
                equivCourseNum
            );
            
            // Get default "Pending" status (CategoryID 1)
            int statusId = getOrCreateStatusId("Pending", 1);
            
            // Insert the form
            String insertSql = "INSERT INTO tcep_form (RequestDate, Term, Year, Degree_Requirement, Core_Designation, " +
                             "StudentID, Incoming_CourseID, Equivalent_CourseID, InstitutionID, StatusID, StartAdvisorID, CurrentAdvisorID) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int formId;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, (java.sql.Date) formData.get("requestDate"));
                ps.setString(2, (String) formData.get("term"));
                ps.setInt(3, (Integer) formData.get("year"));
                ps.setString(4, (String) formData.get("degreeRequirement"));
                ps.setString(5, (String) formData.get("coreDesignation"));
                ps.setInt(6, studentId);
                ps.setInt(7, incomingCourseId);
                if (equivalentCourseId != null) {
                    ps.setInt(8, equivalentCourseId);
                } else {
                    ps.setNull(8, java.sql.Types.INTEGER);
                }
                ps.setInt(9, institutionId);
                ps.setInt(10, statusId);
                
                Integer advisorId = (Integer) formData.get("advisorId");
                if (advisorId != null) {
                    ps.setInt(11, advisorId);
                    ps.setInt(12, advisorId);
                } else {
                    ps.setNull(11, java.sql.Types.INTEGER);
                    ps.setNull(12, java.sql.Types.INTEGER);
                }
                
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        formId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to create form, no ID obtained");
                    }
                }
            }
            
            // Add initial status history
            addStatusHistory(formId, statusId, "Form created", null);
            
            conn.commit();
            return formId;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Updates an existing TCEP form and related records
     * @param formId The FormID to update
     * @param formData Map containing all form fields to update
     * @throws SQLException if database error occurs
     * Written by Davis Huynh (dxh170005)
     * 
     * Expected keys in formData Map (all optional except those marked required):
     * - studentName (String)
     * - utdId (String)
     * - netId (String)
     * - incomingCourseName (String)
     * - incomingCourseNumber (String)
     * - institutionName (String)
     * - equivalentCourseNumber (String)
     * - degreeRequirement (String)
     * - coreDesignation (String)
     * - term (String: Fall/Spring/Summer/Other)
     * - year (Integer)
     */
    public static void updateForm(int formId, Map<String, Object> formData) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        
        try {
            System.out.println("DEBUG: Updating form " + formId);
            System.out.println("DEBUG: Form data keys: " + formData.keySet());
            
            // Get current form data to find existing foreign keys
            String selectSql = "SELECT StudentID, Incoming_CourseID, InstitutionID, Equivalent_CourseID FROM tcep_form WHERE FormID = ?";
            int studentId, incomingCourseId, institutionId;
            Integer equivalentCourseId = null;
            
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, formId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Form not found: " + formId);
                    }
                    studentId = rs.getInt("StudentID");
                    incomingCourseId = rs.getInt("Incoming_CourseID");
                    institutionId = rs.getInt("InstitutionID");
                    equivalentCourseId = rs.getObject("Equivalent_CourseID") != null ? rs.getInt("Equivalent_CourseID") : null;
                    System.out.println("DEBUG: Current StudentID=" + studentId + ", Incoming_CourseID=" + incomingCourseId + ", InstitutionID=" + institutionId);
                }
            }
            
            // Update or create related records if data is provided
            if (formData.containsKey("studentName") || formData.containsKey("netId") || formData.containsKey("utdId")) {
                System.out.println("DEBUG: Updating student record...");
                studentId = getOrCreateStudent(
                    (String) formData.get("studentName"),
                    (String) formData.get("utdId"),
                    (String) formData.get("netId"),
                    (Integer) formData.get("advisorId")
                );
                System.out.println("DEBUG: Updated StudentID=" + studentId);
            }
            
            if (formData.containsKey("incomingCourseName") || formData.containsKey("incomingCourseNumber")) {
                System.out.println("DEBUG: Updating incoming course record...");
                
                // Get equivalent course number for the foreign key
                String equivCourseNum = (String) formData.get("equivalentCourseNumber");
                if (equivCourseNum == null || equivCourseNum.trim().isEmpty()) {
                    equivCourseNum = "UNKN0000";
                }
                
                incomingCourseId = getOrCreateIncomingCourse(
                    (String) formData.get("incomingCourseName"),
                    (String) formData.get("incomingCourseNumber"),
                    institutionId,
                    (Integer) formData.get("departmentId"),
                    equivCourseNum
                );
                System.out.println("DEBUG: Updated Incoming_CourseID=" + incomingCourseId);
            }
            
            if (formData.containsKey("institutionName")) {
                System.out.println("DEBUG: Updating institution record...");
                institutionId = getOrCreateInstitution(
                    (String) formData.get("institutionName")
                );
                System.out.println("DEBUG: Updated InstitutionID=" + institutionId);
            }
            
            if (formData.containsKey("equivalentCourseNumber")) {
                String equivCourseNum = (String) formData.get("equivalentCourseNumber");
                if (equivCourseNum != null && !equivCourseNum.trim().isEmpty()) {
                    System.out.println("DEBUG: Updating equivalent course record...");
                    equivalentCourseId = getOrCreateEquivalentCourse(equivCourseNum);
                    System.out.println("DEBUG: Updated Equivalent_CourseID=" + equivalentCourseId);
                } else {
                    equivalentCourseId = null;
                }
            }
            
            // Build dynamic UPDATE statement
            StringBuilder updateSql = new StringBuilder("UPDATE tcep_form SET ");
            updateSql.append("StudentID = ?, Incoming_CourseID = ?, InstitutionID = ?, Equivalent_CourseID = ?");
            
            if (formData.containsKey("degreeRequirement")) {
                updateSql.append(", Degree_Requirement = ?");
            }
            if (formData.containsKey("coreDesignation")) {
                updateSql.append(", Core_Designation = ?");
            }
            if (formData.containsKey("term")) {
                updateSql.append(", Term = ?");
            }
            if (formData.containsKey("year")) {
                updateSql.append(", Year = ?");
            }
            
            updateSql.append(" WHERE FormID = ?");
            
            System.out.println("DEBUG: Update SQL: " + updateSql.toString());
            
            try (PreparedStatement ps = conn.prepareStatement(updateSql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, studentId);
                ps.setInt(paramIndex++, incomingCourseId);
                ps.setInt(paramIndex++, institutionId);
                if (equivalentCourseId != null) {
                    ps.setInt(paramIndex++, equivalentCourseId);
                } else {
                    ps.setNull(paramIndex++, java.sql.Types.INTEGER);
                }
                
                if (formData.containsKey("degreeRequirement")) {
                    ps.setString(paramIndex++, (String) formData.get("degreeRequirement"));
                }
                if (formData.containsKey("coreDesignation")) {
                    ps.setString(paramIndex++, (String) formData.get("coreDesignation"));
                }
                if (formData.containsKey("term")) {
                    ps.setString(paramIndex++, (String) formData.get("term"));
                }
                if (formData.containsKey("year")) {
                    ps.setInt(paramIndex++, (Integer) formData.get("year"));
                }
                
                ps.setInt(paramIndex, formId);
                int rowsUpdated = ps.executeUpdate();
                System.out.println("DEBUG: Rows updated in tcep_form: " + rowsUpdated);
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
