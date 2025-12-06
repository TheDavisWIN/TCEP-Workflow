/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.controlsfx.control.CheckComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.data.TCEPForm;
import utd.tcep.data.TCEPFormTable;
import utd.tcep.data.TCEPUser;
import utd.tcep.main.TCEPWorkflowApp;
import utd.tcep.db.TCEPDatabaseService;

public class FormDetailedController {

    private TCEPForm currentForm;
    private Runnable onStatusChangeCallback;
    private Runnable onNavigateToTableCallback;
    private String advisorName;
    private String firstName;
    private String lastName;
    private String middleName;
    private boolean loadingForm = false;
    private String currentStudentDepartment = null;
    private final Map<String, Integer> recipientNameToId = new HashMap<>();
    private Connection connection;
    private TCEPUser currentUser;
    private FormTableController formTableController;
    private NavigationController navigationController;
    private boolean shouldNavigateToTableOnClose = false;
    private Node currentHistoryView;

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
    
    @FXML private Label formStartedByLabel;
    @FXML private Label startedDateLabel;
    
    @FXML private VBox formViewContainer;
    @FXML private Button viewHistoryButton;
    @FXML private Button acceptButton;
    @FXML private Button denyButton;
    @FXML private Button sendBackButton;
    @FXML private Button generatePdfButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;
    @FXML private Button exportButton;
    @FXML private Button pdfCancelButton;
    @FXML private Button verifyEquivalencyButton;
    @FXML private TextArea verificationResultsArea;
    @FXML private HBox overlayContainerBackground;
    @FXML private VBox overlayContainer;
    @FXML private ComboBox<String> sendBackReasonCombo;
    @FXML private CheckComboBox<String> sendBackRecipientCombo;
    @FXML private ComboBox<String> approvalReasonCombo;
    @FXML private CheckComboBox<String> approvalRecipientCombo;
    @FXML private ComboBox<String> denialReasonCombo;
    @FXML private CheckComboBox<String> denialRecipientCombo;
    @FXML private TextField approvalReasonOtherField;
    @FXML private TextField denialReasonOtherField;
    @FXML private TextField sendBackReasonOtherField;
    @FXML private Button confirmApprovalButton;
    @FXML private Button confirmDenialButton;
    @FXML private Button confirmSendBackButton;
    @FXML private Button generatePdfFromFeedbackButton;
    @FXML private ImageView previewImageView;
    @FXML private Label statusIcon;
    @FXML private Label statusLabel;
    @FXML private Label resultTitle;
    @FXML private Label resultText;
    
    

    // Setup property listeners for fields in the form
    // Written by Ryan Pham (rkp200003)
    @FXML
    public void initialize() {
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!loadingForm) {
                firstName = newValue;
                updateStudentName();
            }
        });
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!loadingForm) {
                lastName = newValue;
                updateStudentName();
            }
        });
        miField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!loadingForm) {
                middleName = newValue;
                updateStudentName();
            }
        });
        studentIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentForm.setUtdId((String)newValue);
        });
        origCourseNumField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Add incoming course suggestions here
        });
        origCourseTitleField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        origCreditHoursField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        sourceInstitutionNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        sourceInstitutionLocationField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        equivalentCourseField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        satisfiedRequirementField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });
        coreDesignationField.textProperty().addListener((observable, oldValue, newValue) -> {
            
        });

        verifyEquivalencyButton.setOnAction(event -> {
            try {
                handleVerifyEquivalency();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        acceptButton.setOnAction(event -> {
            try {
                handleAccept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        denyButton.setOnAction(event -> {
            try {
                handleDeny();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendBackButton.setOnAction(event -> {
            try {
                handleSendBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        generatePdfButton.setOnAction(event -> {
            String templatePath = "Blank TCEP.pdf";
            showPDFPreview(templatePath);
        });
        editButton.setOnAction(event -> handleEdit());
        saveButton.setOnAction(event -> {
            try {
                handleSave();
            } catch (SQLException e) {
                System.err.println("Error saving form: " + e.getMessage());
                e.printStackTrace();
            }
        });
        cancelButton.setOnAction(event -> handleCancel());
        deleteButton.setOnAction(event -> {
            try {
                handleDelete();
            } catch (SQLException e) {
                System.err.println("Error deleting form: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Initialize button visibility - start with edit mode buttons hidden
        updateButtonVisibility();
        
        // Disable all form fields by default until Edit mode is enabled
        setFieldsEditable(false);
    }

    @FXML
    private void handleViewHistory() {
        try {
            URL fxmlUrl = TCEPWorkflowApp.class.getResource("/utd/tcep/formhistoryview.fxml");
            if (fxmlUrl == null) {
                new Alert(Alert.AlertType.ERROR, "formhistoryview.fxml not found!").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node historyRoot = loader.load();
            TCEPHistoryController controller = loader.getController();
            controller.setFormData(currentForm.getId(), this);

            GridPane grid = navigationController.getAppGridPane();
            grid.getChildren().removeIf(n -> GridPane.getColumnIndex(n) != null && GridPane.getColumnIndex(n) == 1);
            grid.add(historyRoot, 1, 0);

            // THIS LINE IS THE KEY — tell NavigationController we're now in "History" mode
            currentHistoryView = historyRoot;

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load history: " + e.getMessage()).showAndWait();
        }
    }

    // Load an overlay FXML into the overlay container and make it visible.
    // Written by Nicolas Hartono (nxh210004)
    public void loadOverlay(String fxmlPath) throws IOException {
        // Clear any existing overlay
        overlayContainer.getChildren().clear();

        // Ensure we have a DB connection for loading advisor lists
        try {
            connection = TCEPDatabaseService.getConnection();
        } catch (SQLException e) {
            // If DB not available, leave connection null and handle later
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource(fxmlPath + ".fxml"));
        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        overlayContainer.getChildren().add(overlayRoot);
        overlayContainerBackground.setVisible(true);

        // Use logged-in advisor ID to exclude from recipient list
        Integer loggedInAdvisorId = (currentUser != null) ? currentUser.getAdvisorId() : null;
        System.out.println("DEBUG loadOverlay: currentUser=" + currentUser + ", loggedInAdvisorId=" + loggedInAdvisorId);
        
        // Get student's department for the department label
        String studentDeptName = null;
        try {
            if (currentForm != null && currentForm.getUtdId() != null && !currentForm.getUtdId().isEmpty()) {
                String sql = "SELECT d.Department_Name FROM student s " +
                           "JOIN department d ON s.DepartmentID = d.DepartmentID " +
                           "WHERE s.UtdID = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(currentForm.getUtdId()));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            studentDeptName = rs.getString("Department_Name");
                        }
                    }
                }
            }
        } catch (NumberFormatException | SQLException e) {
            // ignore parsing or SQL errors here; recipient lists will be built without exclusions
            e.printStackTrace();
        }

        // populate send-back combo boxes if they exist in the loaded FXML
        if (sendBackReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Incomplete information on the syllabus",
                "Different Programming Language",
                "Different Credit Hours",
                "Other"
            );
            sendBackReasonCombo.setItems(reasons);

            // Sets the first index as default
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

        // Populate the send-back recipient check combo from the Advisor table (exclude the logged-in advisor)
        if (sendBackRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList();
            // Now fetch advisors, excluding loggedInAdvisorId if known
            String sqlAll = loggedInAdvisorId == null
                ? "SELECT AdvisorID, Advisor_Name FROM Advisor"
                : "SELECT AdvisorID, Advisor_Name FROM Advisor WHERE AdvisorID <> ?";

            try (PreparedStatement stmt = connection.prepareStatement(sqlAll)) {
                if (loggedInAdvisorId != null) {
                    stmt.setInt(1, loggedInAdvisorId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("Advisor_Name");
                        recipients.add(name);
                        recipientNameToId.put(name, rs.getInt("AdvisorID"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Add a single department label: prefer the student's department name, otherwise fall back to "CS Department"
            String deptLabel = (studentDeptName != null && !studentDeptName.isEmpty()) ? studentDeptName : "CS Department";
            currentStudentDepartment = (studentDeptName != null && !studentDeptName.isEmpty()) ? studentDeptName : null;
            if (!recipients.contains(deptLabel)) recipients.add(deptLabel);

            // populate the CheckComboBox items
            sendBackRecipientCombo.getItems().setAll(recipients);
        }

        // populate approval combos if present
        if (approvalReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Syllabus confirmed",
                "Grade validated",
                "Other"
            );
            approvalReasonCombo.setItems(reasons);

            // Sets the first index as default
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

        // Populate the approval recipient check combo if they exist in the loaded FXML
        if (approvalRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList();
            // Now fetch advisors, excluding loggedInAdvisorId if known
            String sqlAll = loggedInAdvisorId == null
                ? "SELECT AdvisorID, Advisor_Name FROM Advisor"
                : "SELECT AdvisorID, Advisor_Name FROM Advisor WHERE AdvisorID <> ?";

            try (PreparedStatement stmt = connection.prepareStatement(sqlAll)) {
                if (loggedInAdvisorId != null) {
                    stmt.setInt(1, loggedInAdvisorId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("Advisor_Name");
                        recipients.add(name);
                        recipientNameToId.put(name, rs.getInt("AdvisorID"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Add a single department label: prefer the student's department name, otherwise fall back to "CS Department"
            String deptLabel = (studentDeptName != null && !studentDeptName.isEmpty()) ? studentDeptName : "CS Department";
            if (!recipients.contains(deptLabel)) recipients.add(deptLabel);
            // CheckComboBox doesn't have setItems(); replace contents via getItems().setAll(...)
            approvalRecipientCombo.getItems().setAll(recipients);

        }

        // populate denial combos if present
        if (denialReasonCombo != null) {
            ObservableList<String> reasons = FXCollections.observableArrayList(
                "Incomplete information on the syllabus",
                "Different Programming Language",
                "Different Credit Hours",
                "Other"
            );
            denialReasonCombo.setItems(reasons);

            // Sets the first index as default
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

        // Populate the denial recipient check combo if they exist in the loaded FXML
        if (denialRecipientCombo != null) {
            ObservableList<String> recipients = FXCollections.observableArrayList();
            // Now fetch advisors, excluding loggedInAdvisorId if known
            String sqlAll = loggedInAdvisorId == null
                ? "SELECT AdvisorID, Advisor_Name FROM Advisor"
                : "SELECT AdvisorID, Advisor_Name FROM Advisor WHERE AdvisorID <> ?";

            try (PreparedStatement stmt = connection.prepareStatement(sqlAll)) {
                if (loggedInAdvisorId != null) {
                    stmt.setInt(1, loggedInAdvisorId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("Advisor_Name");
                        recipients.add(name);
                        recipientNameToId.put(name, rs.getInt("AdvisorID"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Add a single department label: prefer the student's department name, otherwise fall back to "CS Department"
            String deptLabel = (studentDeptName != null && !studentDeptName.isEmpty()) ? studentDeptName : "CS Department";
            if (!recipients.contains(deptLabel)) recipients.add(deptLabel);
            // CheckComboBox doesn't have setItems(); replace contents via getItems().setAll(...)
            denialRecipientCombo.getItems().setAll(recipients);
            
        }
        
        // Wire up the confirm buttons in the overlays
        if (confirmApprovalButton != null) {
            confirmApprovalButton.setOnAction(event -> {
                try {
                    confirmApproval();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        
        if (confirmDenialButton != null) {
            confirmDenialButton.setOnAction(event -> {
                try {
                    confirmDenial();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        
        if (confirmSendBackButton != null) {
            confirmSendBackButton.setOnAction(event -> {
                try {
                    confirmSendBack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    // Set the current logged-in user
    public void setCurrentUser(TCEPUser user) {
        this.currentUser = user;
    }

    // Set the form table controller reference to allow removing forms
    public void setFormTableController(FormTableController controller) {
        this.formTableController = controller;
    }

    // Set the navigation controller reference to allow switching views
    public void setNavigationController(NavigationController controller) {
        this.navigationController = controller;
    }

    // Close and remove any overlay
    // Written by Nicolas Hartono (nxh210004)
    public void closeOverlay() {
        overlayContainer.getChildren().clear();
        overlayContainerBackground.setVisible(false);
        
        // Navigate back to form table view after closing overlay if flag is set
        if (shouldNavigateToTableOnClose && navigationController != null) {
            shouldNavigateToTableOnClose = false; // Reset flag
            try {
                navigationController.swapView(NavigationController.View.Table);
            } catch (Exception e) {
                System.err.println("Error navigating to table view: " + e.getMessage());
            }
        }
    }

    //Handle verification of course equivalency by checkin database for institution, courses
    //Written by Ayden Benel (acb210000)
    @FXML
    private void handleVerifyEquivalency() throws IOException {
        String institution = sourceInstitutionNameField.getText().trim();
        String incomingCourse = origCourseNumField.getText().trim();
        String utdCourse = equivalentCourseField.getText().trim();
        
        if (institution.isEmpty() || incomingCourse.isEmpty() || utdCourse.isEmpty()) {
            showVerificationError("Please fill in all required fields:\n" +
                                 "- Source Institution Name\n" +
                                 "- Original Course Number\n" +
                                 "- Equivalent Course (UTD)");
            return;
        }
        
        loadOverlay("/utd/tcep/formverificationview");
        
        String report = TCEPDatabaseService.verifyEquivalency(institution, incomingCourse, utdCourse);
        
        // Display results in the TextArea
        if (verificationResultsArea != null) {
            verificationResultsArea.setText(report);
            
            updateVerificationStatus(report);
        }
    }

    private void updateVerificationStatus(String report) {
        if (statusIcon == null || statusLabel == null) return;
        
        //this counts check marks and X marks since thats what expresses if it exists or not
        long checkCount = report.chars().filter(ch -> ch == '✔').count();
        long xCount = report.chars().filter(ch -> ch == '✘').count();
        
        if (xCount == 0 && checkCount == 3) {
            //all good

            statusIcon.setText("✔");
            statusIcon.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px;");
            statusLabel.setText("All verification checks passed! This equivalency exists in the database");
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else if (xCount > 0) {
            //something failed
            statusIcon.setText("✘");
            statusIcon.setStyle("-fx-text-fill: #F44336; -fx-font-size: 18px;");
            statusLabel.setText("Verification failed " + xCount + " check(s), It doesnt exist in the database");
            statusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            
        } else {
            //somehthing is making it not fail nor pass
            statusIcon.setText("⚠");
            statusIcon.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 18px;");
            statusLabel.setText("Verification complete with warnings, double check databse or spelling");
            statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
    }

    //error since all three fields gotta be inserted
    private void showVerificationError(String message) throws IOException {
        loadOverlay("/utd/tcep/formverificationview");
        
        if (verificationResultsArea != null) {
            verificationResultsArea.setText("VERIFICATION ERROR\n\n" + 
                                           message);
        }
        
        if (statusIcon != null && statusLabel != null) {
            statusIcon.setText("⚠");
            statusIcon.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 18px;");
            statusLabel.setText("Missing required information");
            statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
    }

    // Set callback to refresh table when status changes
    // Written by Davis Huynh (dxh170005)
    public void setOnStatusChangeCallback(Runnable callback) {
        this.onStatusChangeCallback = callback;
    }

    // Set callback to navigate to table view
    // Written by Davis Huynh (dxh170005)
    public void setOnNavigateToTableCallback(Runnable callback) {
        this.onNavigateToTableCallback = callback;
    }

    /**
     * Reset edit mode when navigating away
     * Written by Davis Huynh (dxh170005)
     */
    public void resetEditMode() {
        if (isEditMode) {
            isEditMode = false;
            setFieldsEditable(false);
            updateButtonVisibility();
        }
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
    // and modified by Ryan Pham (rkp200003) to add confirmation feedback
    @FXML
    public void confirmApproval() throws IOException {
        String reason = approvalReasonCombo == null ? null : approvalReasonCombo.getValue();
        if ("Other".equals(reason) && approvalReasonOtherField != null) {
            reason = approvalReasonOtherField.getText();
            // Validate that Other text field is not empty
            if (reason == null || reason.trim().isEmpty()) {
                System.err.println("Please provide a reason in the text field when 'Other' is selected.");
                return;
            }
        }
        
        // Get selected recipients from CheckComboBox
        java.util.List<String> recipients = approvalRecipientCombo == null 
            ? java.util.Collections.emptyList() 
            : approvalRecipientCombo.getCheckModel().getCheckedItems();
        
        // Validate that at least one recipient is selected
        if (recipients.isEmpty()) {
            System.err.println("Please select at least one recipient before approving.");
            return;
        }
        
        // Save approval to database
        if (currentForm != null && currentForm.getId() > 0) {
            try {
                // Get or create "Approved" status (CategoryID 2 = Approved)
                int approvedStatusId = TCEPDatabaseService.getOrCreateStatusId("Approved", 2);
                
                String formattedReason = String.format("Approved%s%s", 
                    reason != null && !reason.trim().isEmpty() ? ": " + reason : "",
                    !recipients.isEmpty() ? " (Recipients: " + String.join(", ", recipients) + ")" : "");
                
                TCEPDatabaseService.addStatusHistory(currentForm.getId(), approvedStatusId, 
                    formattedReason, null);
                TCEPDatabaseService.updateFormStatus(currentForm.getId(), approvedStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(approvedStatusId));
                System.out.println("Approval confirmed and saved. Reason: " + reason + ", Recipients: " + recipients);
                
                // Process each recipient
                processRecipientsForAction(recipients, "Approved", formattedReason);
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save approval: " + e.getMessage());
            }
        }

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) 
            ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formapprovalview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        closeOverlay();

        // Show confirmation feedback overlay (Written by Ryan Pham (rkp200003))
        loadOverlay("/utd/tcep/formdecisionfeedbackoverlay");
        resultTitle.setText("Form Accepted");
        resultText.setText("The form has been accepted.");
        
        // Show Generate PDF button if department is selected
        if (showingPDF && generatePdfFromFeedbackButton != null) {
            generatePdfFromFeedbackButton.setVisible(true);
            generatePdfFromFeedbackButton.setManaged(true);
        } else {
            // Set flag to navigate to table when overlay is closed (not showing PDF)
            shouldNavigateToTableOnClose = true;
        }
    }

    // Changes made to denial action
    // Written by Nicolas Hartono (nxh210004)
    // and modified by Ryan Pham (rkp200003) to add confirmation feedback
    @FXML
    public void confirmDenial() throws IOException {
        String reason = denialReasonCombo == null ? null : denialReasonCombo.getValue();
        if ("Other".equals(reason) && denialReasonOtherField != null) {
            reason = denialReasonOtherField.getText();
            // Validate that Other text field is not empty
            if (reason == null || reason.trim().isEmpty()) {
                System.err.println("Please provide a reason in the text field when 'Other' is selected.");
                return;
            }
        }
        
        // Get selected recipients from CheckComboBox
        java.util.List<String> recipients = denialRecipientCombo == null 
            ? java.util.Collections.emptyList() 
            : denialRecipientCombo.getCheckModel().getCheckedItems();
        
        // Validate that at least one recipient is selected
        if (recipients.isEmpty()) {
            System.err.println("Please select at least one recipient before denying.");
            return;
        }
        
        System.out.println("Denial confirmed. Reason: " + reason + ", Recipients: " + recipients);

        // Save denial reason to database
        if (currentForm != null && currentForm.getId() > 0 && reason != null && !reason.trim().isEmpty()) {
            try {
                // Get or create "Denied" status (CategoryID 3 = Denied)
                int deniedStatusId = TCEPDatabaseService.getOrCreateStatusId("Denied", 3);
                TCEPDatabaseService.addStatusHistory(currentForm.getId(), deniedStatusId, 
                    "Denied: " + reason + " (Recipients: " + String.join(", ", recipients) + ")", null);
                TCEPDatabaseService.updateFormStatus(currentForm.getId(), deniedStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(deniedStatusId));
                System.out.println("Denial reason saved to database");
                
                // Process each recipient
                processRecipientsForAction(recipients, "Denied", "Denied: " + reason);
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save denial reason: " + e.getMessage());
            }
        }

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) 
            ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        System.out.println("DEBUG: deptLabel=" + deptLabel + ", showingPDF=" + showingPDF);
        System.out.println("DEBUG: formTableController=" + formTableController + ", currentForm=" + currentForm);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
            // Navigate back to form table view
            if (navigationController != null) {
                System.out.println("Navigating back to table view");
            }
        } else if (!showingPDF) {
            System.out.println("DEBUG: Not removing form - formTableController or currentForm is null");
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdenialview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        closeOverlay();

        // Show confirmation feedback overlay (Written by Ryan Pham (rkp200003))
        loadOverlay("/utd/tcep/formdecisionfeedbackoverlay");
        resultTitle.setText("Form Denied");
        resultText.setText("The form has been denied. You may now close this window.");
        
        // Show Generate PDF button if department is selected
        if (showingPDF && generatePdfFromFeedbackButton != null) {
            generatePdfFromFeedbackButton.setVisible(true);
            generatePdfFromFeedbackButton.setManaged(true);
        } else {
            // Set flag to navigate to table when overlay is closed (not showing PDF)
            shouldNavigateToTableOnClose = true;
        }
    }

    // Changes made to send back action
    // Written by Nicolas Hartono (nxh210004)
    // Updated by Davis Huynh (dxh170005) to save reason to database
    // and modified by Ryan Pham (rkp200003) to add confirmation feedback
    @FXML
    public void confirmSendBack() throws IOException {
        String reason = sendBackReasonCombo == null ? null : sendBackReasonCombo.getValue();
        if ("Other".equals(reason) && sendBackReasonOtherField != null) {
            reason = sendBackReasonOtherField.getText();
            if (reason == null || reason.trim().isEmpty()) {
                System.err.println("Please provide a reason in the text field when 'Other' is selected.");
                return;
            }
        }
        
        // Get selected recipients from CheckComboBox
        java.util.List<String> recipients = sendBackRecipientCombo == null 
            ? java.util.Collections.emptyList() 
            : sendBackRecipientCombo.getCheckModel().getCheckedItems();
        
        // Validate that at least one recipient is selected
        if (recipients.isEmpty()) {
            System.err.println("Please select at least one recipient before sending back.");
            return;
        }
        
        System.out.println("Send back confirmed. Reason: " + reason + ", Recipients: " + recipients);

        // Save send back reason to database
        if (currentForm != null && currentForm.getId() > 0 && reason != null && !reason.trim().isEmpty()) {
            try {
                // Get or create "Sent Back" status (CategoryID 4 = Sent Back)
                int sentBackStatusId = TCEPDatabaseService.getOrCreateStatusId("Sent Back", 4);
                TCEPDatabaseService.addStatusHistory(currentForm.getId(), sentBackStatusId, 
                    "Sent Back: " + reason + " (Recipients: " + String.join(", ", recipients) + ")", null);
                TCEPDatabaseService.updateFormStatus(currentForm.getId(), sentBackStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(sentBackStatusId));
                System.out.println("Send back reason saved to database");
                
                // Process each recipient
                processRecipientsForAction(recipients, "Sent Back", "Sent Back: " + reason);
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save send back reason: " + e.getMessage());
            }
        }

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) 
            ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        System.out.println("DEBUG: deptLabel=" + deptLabel + ", showingPDF=" + showingPDF);
        System.out.println("DEBUG: formTableController=" + formTableController + ", currentForm=" + currentForm);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
            // Navigate back to form table view
            if (navigationController != null) {
                System.out.println("Navigating back to table view");
            }
        } else if (!showingPDF) {
            System.out.println("DEBUG: Not removing form - formTableController or currentForm is null");
        }
        
        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formsendbackview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();
        closeOverlay();

        // Show confirmation feedback overlay (Written by Ryan Pham (rkp200003))
        loadOverlay("/utd/tcep/formdecisionfeedbackoverlay");
        resultTitle.setText("Form Sent Back");
        resultText.setText("The form has been sent back. You may now close this window.");
        
        // Show Generate PDF button if department is selected
        if (showingPDF && generatePdfFromFeedbackButton != null) {
            generatePdfFromFeedbackButton.setVisible(true);
            generatePdfFromFeedbackButton.setManaged(true);
        } else {
            // Set flag to navigate to table when overlay is closed (not showing PDF)
            shouldNavigateToTableOnClose = true;
        }
    }
    
    // Helper method to process multiple recipients for an action
    // Iterates through selected recipients and records action in database
    // Written by Nicolas Hartono (nxh210004)
    private void processRecipientsForAction(java.util.List<String> recipients, String actionType, String comments) {
        if (recipients == null || recipients.isEmpty() || currentForm == null) {
            return;
        }
        
        int formId = currentForm.getId();
        System.out.println("Processing " + recipients.size() + " recipients for action: " + actionType);
        
        // Get status ID (2=Approved, 3=Denied, 4=Sent Back)
        int newStatusId = actionType.equals("Approved") ? 2 : (actionType.equals("Denied") ? 3 : 4);
        
        // Update form status in database
        try {
            TCEPDatabaseService.updateFormStatus(formId, newStatusId);
        } catch (SQLException e) {
            System.err.println("Error updating form status: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Create status history entry for each recipient advisor
        for (String recipientName : recipients) {
            // Check if this is a department label (not an advisor name)
            if (recipientName.contains("Department")) {
                System.out.println("Skipping department label: " + recipientName);
                continue;
            }
            
            // Look up advisor ID from the map
            Integer advisorId = recipientNameToId.get(recipientName);
            if (advisorId != null) {
                System.out.println("Creating status history for recipient: " 
                    + recipientName + " (ID: " + advisorId + ")");
                try {
                    // Create status history entry - this makes the form appear in recipient's list
                    TCEPDatabaseService.addStatusHistoryWithAdvisor(formId, newStatusId, comments, advisorId, null);
                    System.out.println("Successfully added status history for advisor " + advisorId);
                } catch (SQLException e) {
                    System.err.println("Error adding status history for advisor " + advisorId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Could not find advisor ID for recipient: " + recipientName);
            }
        }
    }

    @FXML
    public void handleGeneratePDF(String templatePath, String outputPath) {
        exportPDF(templatePath, outputPath);
    }
    
    // Handle Generate PDF button click from confirmation feedback overlay
    @FXML
    public void handleGeneratePDFFromFeedback() {
        String templatePath = "Blank TCEP.pdf";
        boolean pdfExported = showPDFPreview(templatePath);
        // Only remove form if PDF was actually exported (not canceled)
        if (pdfExported && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table after PDF export");
            formTableController.removeFormFromUI(currentForm);
        }
        // Navigate back to form list after PDF dialog is closed
        if (navigationController != null) {
            navigationController.swapView(NavigationController.View.Table);
        }
    }

    // Show PDF preview in a dialog before exporting
    // Written by Davis Huynh (dxh170005)
    // Returns true if PDF was exported, false if canceled
    private boolean showPDFPreview(String templatePath) {
        final boolean[] pdfExported = {false};
        try {
            PDDocument pdfDoc = generatePDFDocument(templatePath);
            if (pdfDoc == null) return false;

            PDFRenderer renderer = new PDFRenderer(pdfDoc);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 150);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/utd/tcep/pdfpreview.fxml"));
            VBox root = loader.load();
            PDFPreviewController previewController = loader.getController();

            previewController.setPreviewImage(fxImage);

            Stage previewStage = new Stage();
            previewStage.initModality(Modality.APPLICATION_MODAL);
            previewStage.setTitle("PDF Preview");

            previewController.getExportButton().setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save PDF");
                
                String firstName = firstNameField.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
                String lastName = lastNameField.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
                String courseNum = equivalentCourseField.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
                String filename = String.format("%s-%s-TCEP-%s.pdf", 
                    firstName.isEmpty() ? "Student" : firstName,
                    lastName.isEmpty() ? "Name" : lastName,
                    courseNum.isEmpty() ? "Course" : courseNum);
                
                fileChooser.setInitialFileName(filename);
                File initialDir = new File(System.getProperty("user.dir") + "/tcepworkflow/TCEP Forms");
                if (initialDir.exists()) {
                    fileChooser.setInitialDirectory(initialDir);
                }
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );
                File file = fileChooser.showSaveDialog(previewStage);
                if (file != null) {
                    try {
                        pdfDoc.save(file.getAbsolutePath());
                        System.out.println("PDF exported successfully to: " + file.getAbsolutePath());
                        pdfExported[0] = true;
                        previewStage.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            previewController.getCancelButton().setOnAction(e -> previewStage.close());

            Scene scene = new Scene(root, 650, 800);
            previewStage.setScene(scene);
            previewStage.showAndWait();

            pdfDoc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfExported[0];
    }

    // Generate PDF document in memory without saving to file
    // Written by Davis Huynh (dxh170005)
    private PDDocument generatePDFDocument(String templatePath) {
        try {
            InputStream templateStream = getClass().getResourceAsStream("/utd/tcep/" + templatePath);
            if (templateStream == null) {
                System.err.println("Could not find PDF template: " + templatePath);
                return null;
            }
            PDDocument pdfDoc = PDDocument.load(templateStream);
            PDAcroForm acroForm = pdfDoc.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                System.err.println("No form found in the PDF template.");
                pdfDoc.close();
                return null;
            }

            String RE = lastNameField.getText() + ", " + (middleName != null ? middleName + ", " : "") + firstNameField.getText();
            fillField(acroForm, "RE", RE);
            fillField(acroForm, "Text8", studentIdField.getText());
            fillField(acroForm, "REQUEST FOR", origCourseNumField.getText());
            fillField(acroForm, "Course Title", origCourseTitleField.getText());
            fillField(acroForm, "of Credit Hours", origCreditHoursField.getText());
            fillField(acroForm, "Taken at", sourceInstitutionNameField.getText());
            fillField(acroForm, "Location", sourceInstitutionLocationField.getText());
            fillField(acroForm, "Transfer as", equivalentCourseField.getText());
            fillField(acroForm, "andor to satisfy", satisfiedRequirementField.getText());
            fillField(acroForm, "Text9", currentForm != null ? currentForm.getStatusReason() : "");
            
            // Add advisor name to PDF
            if (advisorName != null && !advisorName.isEmpty()) {
                fillField(acroForm, "By", advisorName);
            }
            
            // Check ECS field (marked as BBS in the PDF)
            fillField(acroForm, "BBS", "X");
            
            // Check approval/denial checkboxes based on status
            if (currentForm != null && currentForm.getStatus() != null) {
                String status = currentForm.getStatus();
                // Status 2 = Approved
                if ("2".equals(status)) {
                    fillField(acroForm, "ASSOCIATE DEAN RESPONSE", "X");
                }
                // Status 3 = Denied
                else if ("3".equals(status)) {
                    fillField(acroForm, "undefined_2", "X");
                }
            }

            acroForm.flatten();
            return pdfDoc;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Clear all fields in the form and reset to blank form state
    // Written by Ryan Pham (rkp200003)
    private void clearForm() {
        clearFormFields();
        
        // Initialize currentForm for new form creation
        if (currentForm == null || currentForm.getId() > 0) {
            currentForm = new TCEPForm(0);
        }
        
        // Set the current logged-in user as the starter
        TCEPUser currentUser = TCEPUser.getCurrentUser();
        if (currentUser != null && currentUser.getAdvisorName() != null) {
            advisorName = currentUser.getAdvisorName();
            if (formStartedByLabel != null) {
                formStartedByLabel.setText("By: " + advisorName);
            }
        } else {
            if (formStartedByLabel != null) {
                formStartedByLabel.setText("By: Unknown");
            }
        }
        
        // Set current date
        if (startedDateLabel != null) {
            startedDateLabel.setText("Started: " + java.time.LocalDate.now().toString());
        }
    }
    
    // Clear only the text fields without resetting currentForm
    // Used when loading existing form data
    // Written by Ryan Pham (rkp200003)
    private void clearFormFields() {
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
        loadingForm = true; // set loadingForm to true to prevent listeners from firing while populating fields

        // Exit edit mode if currently in edit mode
        if (isEditMode) {
            isEditMode = false;
            setFieldsEditable(false);
            updateButtonVisibility();
        }
        
        currentForm = form;

        // Only clear fields visually, don't reset currentForm
        clearFormFields();

        // Only load from database if the form actually exists in database
        // Check if form exists before loading to avoid loading wrong data for blank forms
        try {
            if (TCEPDatabaseService.getFormDataById(form.getId()) != null) {
                loadByID(form.getId());
            } else {
                System.out.println("Form ID " + form.getId() + " not found in database, treating as blank form");
            }
        } catch (SQLException e) {
            System.err.println("Error loading form data: " + e.getMessage());
            e.printStackTrace();
        }
        
        loadingForm = false;
    }

    // Written by Ryan Pham (rkp200003)
    public TCEPForm getForm() {
        return currentForm;
    }

    // Load form data from database by ID (FormID) and populate fields
    // Written by Davis Huynh (dxh170005)
    public void loadByID(int id) throws SQLException{
        // System.out.println("loadByID called with id: " + id);
        
        // Use database service to retrieve form data
        Map<String, Object> formData = TCEPDatabaseService.getFormDataById(id);
        
        if (formData != null) {
            System.out.println("Found matching record in database");
            
            if (currentForm == null) {
                currentForm = new TCEPForm((Integer) formData.get("FormID"));
            }

            currentForm.setId(id);
            System.out.println("DEBUG: Set currentForm.FormId = " + currentForm.getId());
            
            Object utdIdObj = formData.get("UtdID");
            String netId = (String) formData.get("NetID");
            studentIdField.setText(netId != null ? netId :
                (utdIdObj != null ? String.valueOf(utdIdObj) : ""));
            
            // Populate name fields by splitting full name
            String studentName = (String) formData.get("StudentName");
            
            if (studentName != null)
            {
                String[] nameSplit = studentName.split(" ");

                for (int i = 0; i < nameSplit.length; i++)
                {
                    if (i == 0)
                    {
                        firstNameField.setText(nameSplit[i]);
                        firstName = nameSplit[i];
                    }
                    else if (i == nameSplit.length - 1)
                    {
                        lastNameField.setText(nameSplit[i]);
                        lastName = nameSplit[i];
                    }
                    else
                    {
                        miField.setText(nameSplit[i]);
                        middleName = nameSplit[i];
                    }
                }
            }
            
            origCourseNumField.setText(formData.get("IncomingCourseNumber") != null ? 
                (String) formData.get("IncomingCourseNumber") : "");
            origCourseTitleField.setText(formData.get("IncomingCourseName") != null ? 
                (String) formData.get("IncomingCourseName") : "");
            sourceInstitutionNameField.setText(formData.get("InstitutionName") != null ? 
                (String) formData.get("InstitutionName") : "");
            equivalentCourseField.setText(formData.get("EquivalentCourseNumber") != null ? 
                (String) formData.get("EquivalentCourseNumber") : "");
            satisfiedRequirementField.setText(formData.get("DegreeRequirement") != null ? 
                (String) formData.get("DegreeRequirement") : "");
            coreDesignationField.setText(formData.get("CoreDesignation") != null ? 
                (String) formData.get("CoreDesignation") : "");
            
            // Set advisor name in the label
            advisorName = (String) formData.get("StartAdvisorName");
            if (formStartedByLabel != null) {
                formStartedByLabel.setText("By: " + (advisorName != null ? advisorName : "Unknown"));
            }
            
            // Set request date in the label
            if (startedDateLabel != null) {
                java.sql.Date requestDate = (java.sql.Date) formData.get("RequestDate");
                if (requestDate != null) {
                    startedDateLabel.setText("Started: " + requestDate.toString());
                } else {
                    startedDateLabel.setText("Started: Unknown");
                }
            }
            
            // System.out.println("Fields populated successfully");
        } else {
            System.out.println("No matching record found for id: " + id);
        }
    }

    // Export the filled form to a PDF file
    // Written by Davis Huynh (dxh170005)
    public void exportPDF(String templatePath, String outputPath) {
        try {
            InputStream templateStream = getClass().getResourceAsStream("/utd/tcep/" + templatePath);
            if (templateStream == null) {
                System.err.println("Could not find PDF template: " + templatePath);
                return;
            }
            PDDocument pdfDoc = PDDocument.load(templateStream);
            PDAcroForm acroForm = pdfDoc.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                System.err.println("No form found in the PDF template.");
                pdfDoc.close();
                return;
            }

            String RE = lastNameField.getText() + ", " + (middleName != null ? middleName + ", " : "") + firstNameField.getText();
            fillField(acroForm, "RE", RE);
            fillField(acroForm, "Text8", studentIdField.getText());
            fillField(acroForm, "REQUEST FOR", origCourseNumField.getText());
            fillField(acroForm, "Course Title", origCourseTitleField.getText());
            fillField(acroForm, "of Credit Hours", origCreditHoursField.getText());
            fillField(acroForm, "Taken at", sourceInstitutionNameField.getText());
            fillField(acroForm, "Location", sourceInstitutionLocationField.getText());
            fillField(acroForm, "Transfer as", equivalentCourseField.getText());
            fillField(acroForm, "andor to satisfy", satisfiedRequirementField.getText());
            fillField(acroForm, "Text9", currentForm != null ? currentForm.getStatusReason() : "");
            
            // Add advisor name to PDF
            if (advisorName != null && !advisorName.isEmpty()) {
                fillField(acroForm, "By", advisorName);
            }
            
            // Check ECS field (marked as BBS in the PDF)
            fillField(acroForm, "BBS", "X");
            
            // Check approval/denial checkboxes based on status
            if (currentForm != null && currentForm.getStatus() != null) {
                String status = currentForm.getStatus();
                // Status 2 = Approved, check "ASSOCIATE DEAN RESPONSE"
                if ("2".equals(status)) {
                    fillField(acroForm, "ASSOCIATE DEAN RESPONSE", "Yes");
                }
                // Status 3 = Denied, check "undefined_2"
                else if ("3".equals(status)) {
                    fillField(acroForm, "undefined_2", "Yes");
                }
            }
            // fillField(acroForm, "coreDesignation", coreDesignationField.getText());

            // Make fields read-only
            acroForm.flatten();

            pdfDoc.save(outputPath);
            pdfDoc.close();

            System.out.println("PDF exported successfully to: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Concatenate first, middle, last names since DB only has one row for name
    // Written by Ryan Pham (rkp200003)
    private void updateStudentName() {
        if (middleName != null && !middleName.isEmpty()) {
            currentForm.setStudentName(firstName + " " + middleName + " " + lastName);
        } else {
            currentForm.setStudentName(firstName + " " + lastName);
        }
    }
    
    // Helper method to fill a field in the PDF form
    // Written by Davis Huynh (dxh170005)
    private void fillField(PDAcroForm acroForm, String fieldName, String value) {
        PDField field = acroForm.getField(fieldName);
        if (field != null) {
            try {
                field.setValue(value != null ? value : "");
            } catch (IOException e) {
                System.err.println("Error setting value for field: " + fieldName);
                e.printStackTrace();
            }
        } else {
            System.err.println("Field not found in PDF form: " + fieldName);
        }
    }

    // Called when navigating away from detailed form view
    // Written by Ryan Pham (rkp200003)
    public void onNavigatedAway() {
        closeOverlay();
    }

    @FXML
    private void testEquivalencyCheck() {
        String report = TCEPDatabaseService.verifyEquivalency(
            "University of North Texas",   // Institution name
            "CSCE 1030",                    // Incoming course number
            "CS 1336"                       // UTD equivalent course number
        );

        System.out.println(report);
    }

    // Track original values for cancel functionality
    private Map<String, String> originalValues = new HashMap<>();
    private boolean isEditMode = false;

    /**
     * Update button visibility based on edit mode state
     * Written by Davis Huynh (dxh170005)
     */
    private void updateButtonVisibility() {
        if (isEditMode) {
            editButton.setDisable(true);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
            viewHistoryButton.setDisable(true);
            generatePdfButton.setDisable(true);
            deleteButton.setDisable(true);
            acceptButton.setDisable(true);
            denyButton.setDisable(true);
            sendBackButton.setDisable(true);
        } else {
            editButton.setDisable(false);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            viewHistoryButton.setDisable(false);
            generatePdfButton.setDisable(false);
            deleteButton.setDisable(false);
            acceptButton.setDisable(false);
            denyButton.setDisable(false);
            sendBackButton.setDisable(false);
        }
    }

    /**
     * Enable edit mode - make all fields editable
     * Written by Davis Huynh (dxh170005)
     */
    @FXML
    private void handleEdit() {
        isEditMode = true;
        
        originalValues.clear();
        originalValues.put("firstName", firstNameField.getText());
        originalValues.put("lastName", lastNameField.getText());
        originalValues.put("mi", miField.getText());
        originalValues.put("studentId", studentIdField.getText());
        originalValues.put("origCourseNum", origCourseNumField.getText());
        originalValues.put("origCourseTitle", origCourseTitleField.getText());
        originalValues.put("origCreditHours", origCreditHoursField.getText());
        originalValues.put("sourceInstitutionName", sourceInstitutionNameField.getText());
        originalValues.put("sourceInstitutionLocation", sourceInstitutionLocationField.getText());
        originalValues.put("equivalentCourse", equivalentCourseField.getText());
        originalValues.put("satisfiedRequirement", satisfiedRequirementField.getText());
        originalValues.put("coreDesignation", coreDesignationField.getText());
        
        setFieldsEditable(true);
        updateButtonVisibility();
        
        System.out.println("Edit mode enabled");
    }

    /**
     * Save changes to the form
     * Written by Davis Huynh (dxh170005)
     */
    @FXML
    private void handleSave() throws SQLException {
        if (!isEditMode) return;
        
        System.out.println("DEBUG: currentForm=" + currentForm);
        System.out.println("DEBUG: currentForm.getId()=" + (currentForm != null ? currentForm.getId() : "null"));
        
        // Validate required fields
        if (studentIdField.getText().trim().isEmpty()) {
            System.err.println("Student ID is required");
            return;
        }
        if (origCourseNumField.getText().trim().isEmpty()) {
            System.err.println("Course number is required");
            return;
        }
        
        // Build form data map
        Map<String, Object> formData = new HashMap<>();
        
        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        formData.put("studentName", fullName);
        formData.put("netId", studentIdField.getText().trim());
        formData.put("incomingCourseName", origCourseTitleField.getText().trim());
        formData.put("incomingCourseNumber", origCourseNumField.getText().trim());
        formData.put("institutionName", sourceInstitutionNameField.getText().trim());
        formData.put("institutionLocation", sourceInstitutionLocationField.getText().trim());
        formData.put("equivalentCourseNumber", equivalentCourseField.getText().trim());
        formData.put("degreeRequirement", satisfiedRequirementField.getText().trim());
        formData.put("coreDesignation", coreDesignationField.getText().trim());
        
        if (currentForm != null && currentForm.getId() > 0) {
            TCEPDatabaseService.updateForm(currentForm.getId(), formData);
            System.out.println("Form updated successfully");
        } else if (currentForm != null && currentForm.getId() == 0) {
            System.out.println("Creating new form...");
            
            formData.put("requestDate", new java.sql.Date(System.currentTimeMillis()));
            formData.put("term", "Fall"); // TODO: Get from UI or default
            formData.put("year", java.time.Year.now().getValue());
            
            // Set the current logged-in advisor as the form starter
            TCEPUser currentUser = TCEPUser.getCurrentUser();
            if (currentUser != null && currentUser.getAdvisorId() != null) {
                formData.put("advisorId", currentUser.getAdvisorId());
                System.out.println("Setting StartAdvisorID to " + currentUser.getAdvisorId());
            }
            
            int newFormId = TCEPDatabaseService.createForm(formData);
            currentForm.setId(newFormId);
            System.out.println("Form created successfully with ID=" + newFormId);
        } else {
            System.err.println("ERROR: Cannot save - currentForm is null or has invalid ID");
            return;
        }
        
        isEditMode = false;
        setFieldsEditable(false);
        updateButtonVisibility();
        if (onStatusChangeCallback != null) {
            onStatusChangeCallback.run();
        }
    }

    /**
     * Cancel edit mode and restore original values
     * Written by Davis Huynh (dxh170005)
     */
    @FXML
    private void handleCancel() {
        if (!isEditMode) return;
        boolean isNewForm = (currentForm != null && currentForm.getId() == 0);

        firstNameField.setText(originalValues.get("firstName"));
        lastNameField.setText(originalValues.get("lastName"));
        miField.setText(originalValues.get("mi"));
        studentIdField.setText(originalValues.get("studentId"));
        origCourseNumField.setText(originalValues.get("origCourseNum"));
        origCourseTitleField.setText(originalValues.get("origCourseTitle"));
        origCreditHoursField.setText(originalValues.get("origCreditHours"));
        sourceInstitutionNameField.setText(originalValues.get("sourceInstitutionName"));
        sourceInstitutionLocationField.setText(originalValues.get("sourceInstitutionLocation"));
        equivalentCourseField.setText(originalValues.get("equivalentCourse"));
        satisfiedRequirementField.setText(originalValues.get("satisfiedRequirement"));
        coreDesignationField.setText(originalValues.get("coreDesignation"));
        
        isEditMode = false;
        setFieldsEditable(false);
        updateButtonVisibility();
        
        System.out.println("Edit mode cancelled");
        
        if (isNewForm && onNavigateToTableCallback != null) {
            onNavigateToTableCallback.run();
        }
    }

    /**
     * Handle delete button click
     * Written by Davis Huynh (dxh170005)
     */
    private void handleDelete() throws SQLException {
        if (currentForm == null || currentForm.getId() <= 0) {
            System.err.println("ERROR: Cannot delete - no form loaded or invalid FormID");
            return;
        }
        
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Form");
        confirmAlert.setHeaderText("Are you sure you want to delete this form?");
        confirmAlert.setContentText("Form ID: " + currentForm.getId() + "\nThis action cannot be undone.");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            TCEPDatabaseService.deleteForm(currentForm.getId());
            if (onStatusChangeCallback != null) {
                onStatusChangeCallback.run();
            }
            System.out.println("Form deleted successfully");
            if (onNavigateToTableCallback != null) {
                onNavigateToTableCallback.run();
            }
        }
    }

    /**
     * Set all form fields editable or read-only
     * Written by Davis Huynh (dxh170005)
     */
    private void setFieldsEditable(boolean editable) {
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        miField.setEditable(editable);
        studentIdField.setEditable(editable);
        origCourseNumField.setEditable(editable);
        origCourseTitleField.setEditable(editable);
        origCreditHoursField.setEditable(editable);
        sourceInstitutionNameField.setEditable(editable);
        sourceInstitutionLocationField.setEditable(editable);
        equivalentCourseField.setEditable(editable);
        satisfiedRequirementField.setEditable(editable);
        coreDesignationField.setEditable(editable);
    }

    public NavigationController getNavigationController() {
        return navigationController;
    }

    public Node getCurrentHistoryView() {
        return currentHistoryView;
    }

    public void clearCurrentHistoryView() {
        currentHistoryView = null;
    }

}

 
