/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
 * Written by Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import utd.tcep.data.TCEPForm;
import utd.tcep.db.TCEPDatabaseService;

public class FormDetailedController {

    private TCEPForm currentForm;
    private String firstName;
    private String lastName;
    private String middleName;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField miField;
    @FXML private TextField studentIdField;
    @FXML private TextField origCourseNumField;
    @FXML private TextField origCourseTitleField;
    @FXML private TextField origCreditHoursField;
    @FXML private TextField sourceInstitutionNameField;
    @FXML private TextField sourceInstitutionLocationField;
    @FXML private TextField equivalentCourseField;
    @FXML private TextField satisfiedRequirementField;
    @FXML private TextField coreDesignationField;
    
    // Setup property listeners for fields in the form
    // Written by Ryan Pham (rkp200003)
    @FXML
    public void initialize() {
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            firstName = newValue;
        });
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            lastName = newValue;
        });
        miField.textProperty().addListener((observable, oldValue, newValue) -> {
            middleName = newValue;
        });
        studentIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentForm.setUtdId((String)newValue);
        });
        origCourseNumField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        origCourseTitleField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        origCreditHoursField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        sourceInstitutionNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        sourceInstitutionLocationField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        equivalentCourseField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        satisfiedRequirementField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
        coreDesignationField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
    }
    // Show form's change history
    @FXML
    private void handleViewHistory() throws IOException {
        
    }

    @FXML
    private void handleAccept() throws IOException {
        
    }

    @FXML
    private void handleDeny() throws IOException {
        
    }

    @FXML
    private void handleSendBack() throws IOException {
        
    }
    
    // Clear all fields in the form
    // Written by Ryan Pham (rkp200003)
    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        miField.setText("");
        studentIdField.setText("");
        origCourseNumField.setText("");
        origCourseTitleField.setText("");
        origCreditHoursField.setText("");
        sourceInstitutionNameField.setText("");
        sourceInstitutionLocationField.setText("");
        equivalentCourseField.setText("");
        satisfiedRequirementField.setText("");
        coreDesignationField.setText("");
    }

    // Set currently managed form and fill all text fields in UI when set
    // Written by Ryan Pham (rkp200003)
    public void setForm(TCEPForm form) {
        currentForm = form;
        String studentID = form.getUtdId();

        clearForm();

        if (form.getStudentName() != null)
        {
            String[] nameSplit = form.getStudentName().split(" ");

            firstNameField.setText(nameSplit[0]);
            lastNameField.setText(nameSplit[1]);
        }

        loadByID(studentID);
    }

    // Written by Ryan Pham (rkp200003)
    public TCEPForm getForm() {
        return currentForm;
    }

    // Load form data from database by student ID and populate fields
    // Written by Davis Huynh (dxh170005)
    public void loadByID(String studentId) {
        String sql = "SELECT f.StudentID, s.Student_Name, f.Degree_Requirement, f.Core_Designation, "
                + "f.Incoming_CourseID, ic.CourseName AS IncomingCourseName, ic.CourseNumber AS IncomingCourseNumber,"
                + "f.Equivalent_CourseID, ec.CourseName AS EquivalentCourseName, "
                + "f.InstitutionID, inst.Institution_Name AS InstitutionName "
                + "FROM tcep_form f "
                + "LEFT JOIN student s ON f.StudentID = s.StudentID "
                + "LEFT JOIN incoming_course ic ON f.Incoming_CourseID = ic.Incoming_CourseID "
                + "LEFT JOIN equivalent_course ec ON f.Equivalent_CourseID = ec.Equivalent_CourseID "
                + "LEFT JOIN institution inst ON f.InstitutionID = inst.InstitutionID "
                + "WHERE f.StudentID = ?";

        try (Connection conn = TCEPDatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                studentIdField.setText(rs.getString("StudentID") != null ? rs.getString("StudentID") : "");
                origCourseNumField.setText(rs.getString("IncomingCourseNumber") != null ? rs.getString("IncomingCourseNumber") : "");
                origCourseTitleField.setText(rs.getString("IncomingCourseName") != null ? rs.getString("IncomingCourseName") : "");
                sourceInstitutionNameField.setText(rs.getString("InstitutionName") != null ? rs.getString("InstitutionName") : "");
                equivalentCourseField.setText(rs.getString("EquivalentCourseName") != null ? rs.getString("EquivalentCourseName") : "");
                satisfiedRequirementField.setText(rs.getString("Degree_Requirement") != null ? rs.getString("Degree_Requirement") : "");
                coreDesignationField.setText(rs.getString("Core_Designation") != null ? rs.getString("Core_Designation") : "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
 