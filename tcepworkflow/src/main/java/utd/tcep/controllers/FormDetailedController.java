/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import utd.tcep.data.TCEPForm;

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
    
    @FXML private VBox formViewContainer;
    @FXML private Button acceptButton;
    @FXML private Button denyButton;
    @FXML private Button sendBackButton;
    @FXML private AnchorPane overlayContainer;
    @FXML private ComboBox<String> sendBackReasonCombo;
    @FXML private ComboBox<String> sendBackRecipientCombo;
    @FXML private ComboBox<String> approvalReasonCombo;
    @FXML private ComboBox<String> approvalRecipientCombo;
    @FXML private ComboBox<String> denialReasonCombo;
    @FXML private ComboBox<String> denialRecipientCombo;
    @FXML private TextField approvalReasonOtherField;
    @FXML private TextField denialReasonOtherField;
    @FXML private TextField sendBackReasonOtherField;
    @FXML private Button confirmApprovalButton;
    @FXML private Button confirmDenialButton;
    @FXML private Button confirmSendBackButton;

    // Setup property listeners for fields in the form
    // Written by Ryan Pham (rkp200003)
    @FXML
    public void initialize() {
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            firstName = newValue;
            updateStudentName();
        });
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            lastName = newValue;
            updateStudentName();
        });
        miField.textProperty().addListener((observable, oldValue, newValue) -> {
            middleName = newValue;
            updateStudentName();
        });
        studentIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentForm.setUtdId((String)newValue);
        });
        origCourseNumField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Add incoming course suggestions here
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
        loadOverlay("/utd/tcep/formsendbackview");
    }
    
    // Changes made to approval action
    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmApproval() throws IOException {
        String reason = approvalReasonCombo == null ? null : approvalReasonCombo.getValue();
        if ("Other".equals(reason) && approvalReasonOtherField != null) {
            reason = approvalReasonOtherField.getText();
        }
        String recipient = approvalRecipientCombo == null ? null : approvalRecipientCombo.getValue();
        System.out.println("Approval confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: persist approval action or update model here
        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formapprovalview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        closeOverlay();
    }

    // Changes made to denial action
    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmDenial() throws IOException {
        String reason = denialReasonCombo == null ? null : denialReasonCombo.getValue();
        if ("Other".equals(reason) && denialReasonOtherField != null) {
            reason = denialReasonOtherField.getText();
        }
        String recipient = denialRecipientCombo == null ? null : denialRecipientCombo.getValue();
        System.out.println("Denial confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: persist denial action or update model here
        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdenialview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        closeOverlay();
    }

    // Changes made to send back action
    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmSendBack() throws IOException {
        String reason = sendBackReasonCombo == null ? null : sendBackReasonCombo.getValue();
        if ("Other".equals(reason) && sendBackReasonOtherField != null) {
            reason = sendBackReasonOtherField.getText();
        }
        String recipient = sendBackRecipientCombo == null ? null : sendBackRecipientCombo.getValue();
        System.out.println("Send back confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: perform any DB updates or messaging here using reason/recipient
        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formsendbackview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();
        closeOverlay();
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

        System.out.println(form.getStudentName());
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

    // Concatenate first, middle, last names since DB only has one row for name
    private void updateStudentName() {
        if (middleName != null && !middleName.isEmpty()) {
            currentForm.setStudentName(firstName + " " + middleName + " " + lastName);
        } else {
            currentForm.setStudentName(firstName + " " + lastName);
        }
    }
}
 
