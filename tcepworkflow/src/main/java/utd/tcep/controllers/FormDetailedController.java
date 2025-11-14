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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.data.TCEPForm;
import utd.tcep.main.TCEPWorkflowApp;
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
    @FXML private Button viewHistoryButton;

    private Node viewHistoryRoot;

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

    private NavigationController navigationController;

    public void setNavigationController(NavigationController nav) {
    this.navigationController = nav;

        // Wire buttons now that nav exists
        if (viewHistoryButton != null) {
            viewHistoryButton.setOnAction(e -> navigationController.swapView(NavigationController.View.History));
        }
        if (acceptButton != null) {
            acceptButton.setOnAction(e -> {
                try { handleAccept(); } catch (IOException ex) { ex.printStackTrace(); }
            });
        }
        if (denyButton != null) {
            denyButton.setOnAction(e -> {
                try { handleDeny(); } catch (IOException ex) { ex.printStackTrace(); }
            });
        }
        if (sendBackButton != null) {
            sendBackButton.setOnAction(e -> {
                try { handleSendBack(); } catch (IOException ex) { ex.printStackTrace(); }
            });
        }
    }
    @FXML
    private void handleViewHistory() {
        navigationController.swapView(NavigationController.View.History);
}

     public VBox getFormViewContainer() {
        return formViewContainer;
    }

    // Load an overlay FXML into the overlay container and make it visible.
    // Written by Nicolas Hartono (nxh210004)
    public void loadOverlay(String fxmlPath) throws IOException {
        // Clear any existing overlay
        overlayContainer.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource(fxmlPath + ".fxml"));
        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        overlayContainer.getChildren().add(overlayRoot);
        overlayContainer.setVisible(true);

        // populate send-back combo boxes if they exist in the loaded FXML
        if (sendBackReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Incomplete information",
                "Missing transcript",
                "Course mismatch",
                "Other"
            );
            sendBackReasonCombo.setItems(reasons);
            if (!reasons.isEmpty()) sendBackReasonCombo.getSelectionModel().selectFirst();
        }

        // show/hide "Other" textfield for send back reason
        if (sendBackReasonCombo != null && sendBackReasonOtherField != null) {
            sendBackReasonCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                boolean show = "Other".equals(newVal);
                sendBackReasonOtherField.setVisible(show);
                sendBackReasonOtherField.setManaged(show);
                if (show) sendBackReasonOtherField.requestFocus();
            });
            String sel = sendBackReasonCombo.getValue();
            boolean show = "Other".equals(sel);
            sendBackReasonOtherField.setVisible(show);
            sendBackReasonOtherField.setManaged(show);
        }

        if (sendBackRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList(
                "Dr. Crynes",
                "Academic Advisor",
                "Department Coordinator",
                "Registrar"
            );
            sendBackRecipientCombo.setItems(recipients);
            if (!recipients.isEmpty()) sendBackRecipientCombo.getSelectionModel().selectFirst();
        }

        // populate approval combos if present
        if (approvalReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Syllabus confirmed",
                "Grade validated",
                "Other"
            );
            approvalReasonCombo.setItems(reasons);
            if (!reasons.isEmpty()) approvalReasonCombo.getSelectionModel().selectFirst();
        }

        // show/hide "Other" textfield for approval reason
        if (approvalReasonCombo != null && approvalReasonOtherField != null) {
            approvalReasonCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                boolean show = "Other".equals(newVal);
                approvalReasonOtherField.setVisible(show);
                approvalReasonOtherField.setManaged(show);
                if (show) approvalReasonOtherField.requestFocus();
            });
            String sel = approvalReasonCombo.getValue();
            boolean show = "Other".equals(sel);
            approvalReasonOtherField.setVisible(show);
            approvalReasonOtherField.setManaged(show);
        }

        if (approvalRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList(
                "Student",
                "Registrar",
                "CS Department"
            );
            approvalRecipientCombo.setItems(recipients);
            if (!recipients.isEmpty()) approvalRecipientCombo.getSelectionModel().selectFirst();
        }

        // populate denial combos if present
        if (denialReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Insufficient grade",
                "Non-equivalent course",
                "Other"
            );
            denialReasonCombo.setItems(reasons);
            if (!reasons.isEmpty()) denialReasonCombo.getSelectionModel().selectFirst();
        }

        // show/hide "Other" textfield for denial reason
        if (denialReasonCombo != null && denialReasonOtherField != null) {
            denialReasonCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                boolean show = "Other".equals(newVal);
                denialReasonOtherField.setVisible(show);
                denialReasonOtherField.setManaged(show);
                if (show) denialReasonOtherField.requestFocus();
            });
            String sel = denialReasonCombo.getValue();
            boolean show = "Other".equals(sel);
            denialReasonOtherField.setVisible(show);
            denialReasonOtherField.setManaged(show);
        }

        if (denialRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList(
                "Student",
                "Department",
                "Registrar"
            );
            denialRecipientCombo.setItems(recipients);
            if (!recipients.isEmpty()) denialRecipientCombo.getSelectionModel().selectFirst();
        }
    }

    // Close and remove any overlay
    // Written by Nicolas Hartono (nxh210004)
    public void closeOverlay() {
        overlayContainer.getChildren().clear();
        overlayContainer.setVisible(false);
    }

    @FXML
    private void handleAccept() throws IOException {
        // load approval overlay instead of using alerts/popups
        loadOverlay("/utd/tcep/formapprovalview");
    }

    @FXML
    private void handleDeny() throws IOException {
        loadOverlay("/utd/tcep/formdenialview");
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
 
