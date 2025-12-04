/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
 * Written by Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Pos;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
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
    @FXML private ImageView previewImageView;
    @FXML private Button exportButton;
    @FXML private Button pdfCancelButton;

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
    // Show form's change history
    @FXML
    private void handleViewHistory() throws IOException {
        
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
        String recipient = approvalRecipientCombo == null ? null : approvalRecipientCombo.getValue();
        
        // Save approval to database
        if (currentForm != null && currentForm.getFormId() > 0) {
            try {
                // Get or create "Approved" status (CategoryID 2 = Approved)
                int approvedStatusId = TCEPDatabaseService.getOrCreateStatusId("Approved", 2);
                
                String formattedReason = String.format("Approved%s%s", 
                    reason != null && !reason.trim().isEmpty() ? ": " + reason : "",
                    recipient != null ? " (Recipient: " + recipient + ")" : "");
                
                TCEPDatabaseService.addStatusHistory(currentForm.getFormId(), approvedStatusId, 
                    formattedReason, null);
                TCEPDatabaseService.updateFormStatus(currentForm.getFormId(), approvedStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(approvedStatusId));
                System.out.println("Approval confirmed and saved. Reason: " + reason + ", Recipient: " + recipient);
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save approval: " + e.getMessage());
            }
        }

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
            // Validate that Other text field is not empty
            if (reason == null || reason.trim().isEmpty()) {
                System.err.println("Please provide a reason in the text field when 'Other' is selected.");
                return;
            }
        }
        String recipient = denialRecipientCombo == null ? null : denialRecipientCombo.getValue();
        System.out.println("Denial confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // Save denial reason to database
        if (currentForm != null && currentForm.getFormId() > 0 && reason != null && !reason.trim().isEmpty()) {
            try {
                // Get or create "Denied" status (CategoryID 3 = Denied)
                int deniedStatusId = TCEPDatabaseService.getOrCreateStatusId("Denied", 3);
                TCEPDatabaseService.addStatusHistory(currentForm.getFormId(), deniedStatusId, 
                    "Denied: " + reason + " (Recipient: " + recipient + ")", null);
                TCEPDatabaseService.updateFormStatus(currentForm.getFormId(), deniedStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(deniedStatusId));
                System.out.println("Denial reason saved to database");
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save denial reason: " + e.getMessage());
            }
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdenialview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();

        closeOverlay();
    }

    // Changes made to send back action
    // Written by Nicolas Hartono (nxh210004)
    // Updated by Davis Huynh (dxh170005) to save reason to database
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
        String recipient = sendBackRecipientCombo == null ? null : sendBackRecipientCombo.getValue();
        System.out.println("Send back confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // Save send back reason to database
        if (currentForm != null && currentForm.getFormId() > 0 && reason != null && !reason.trim().isEmpty()) {
            try {
                // Get or create "Sent Back" status (CategoryID 4 = Sent Back)
                int sentBackStatusId = TCEPDatabaseService.getOrCreateStatusId("Sent Back", 4);
                TCEPDatabaseService.addStatusHistory(currentForm.getFormId(), sentBackStatusId, 
                    "Sent Back: " + reason + " (Recipient: " + recipient + ")", null);
                TCEPDatabaseService.updateFormStatus(currentForm.getFormId(), sentBackStatusId);
                currentForm.setStatusReason(reason);
                currentForm.setStatus(String.valueOf(sentBackStatusId));
                System.out.println("Send back reason saved to database");
                
                // Refresh table to reflect status change
                if (onStatusChangeCallback != null) {
                    onStatusChangeCallback.run();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to save send back reason: " + e.getMessage());
            }
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formsendbackview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        Node overlayRoot = loader.load();
        closeOverlay();
    }

    @FXML
    public void handleGeneratePDF(String templatePath, String outputPath) {
        exportPDF(templatePath, outputPath);
    }

    // Show PDF preview in a dialog before exporting
    // Written by Davis Huynh (dxh170005)
    private void showPDFPreview(String templatePath) {
        try {
            PDDocument pdfDoc = generatePDFDocument(templatePath);
            if (pdfDoc == null) return;

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
                // Status 4 = Approved, check "ASSOCIATE DEAN RESPONSE"
                if ("4".equals(status)) {
                    fillField(acroForm, "ASSOCIATE DEAN RESPONSE", "X");
                }
                // Status 2 = Denied, check "undefined_2"
                else if ("2".equals(status)) {
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
        
        // Initialize currentForm for new form creation
        if (currentForm == null || currentForm.getFormId() > 0) {
            currentForm = new TCEPForm();
            currentForm.setFormId(0); // Mark as new form
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

    // Set currently managed form and fill all text fields in UI when set
    // Written by Ryan Pham (rkp200003)
    public void setForm(TCEPForm form) {
        // Exit edit mode if currently in edit mode
        if (isEditMode) {
            isEditMode = false;
            setFieldsEditable(false);
            updateButtonVisibility();
        }
        
        currentForm = form;
        String utdID = form.getUtdId();
        String netID = form.getNetId();
        // System.out.println("UtdID: " + utdID + ", NetID: " + netID);

        clearForm();

        if (form.getStudentName() != null)
        {
            String[] nameSplit = form.getStudentName().split(" ");

            firstNameField.setText(nameSplit[0]);
            lastNameField.setText(nameSplit[1]);
        }

        if (netID != null && !netID.isEmpty()) {
            // System.out.println("Loading by NetID: " + netID);
            try {
                loadByID(netID);
            } catch (SQLException e) {
                System.err.println("Error loading form data:");
                e.printStackTrace();
            }
        }
        else if (utdID != null && !utdID.isEmpty()) {
            // System.out.println("Loading by UtdID: " + utdID);
            try {
                loadByID(utdID);
            } catch (SQLException e) {
                System.err.println("Error loading form data:");
                e.printStackTrace();
            }
        } else {
            System.out.println("No valid student identifier found.");
            // Start in edit mode when creating a new form
            if (currentForm != null && currentForm.getFormId() == 0) {
                handleEdit();
            }
        }
    }

    // Written by Ryan Pham (rkp200003)
    public TCEPForm getForm() {
        return currentForm;
    }

    // Load form data from database by ID (NetID or UTDID) and populate fields
    // Written by Davis Huynh (dxh170005)
    public void loadByID(String id) throws SQLException{
        // System.out.println("loadByID called with id: " + id);
        
        // Use database service to retrieve form data
        Map<String, Object> formData = TCEPDatabaseService.getFormDataById(id);
        
        if (formData != null) {
            System.out.println("Found matching record in database");
            
            if (currentForm == null) {
                currentForm = new TCEPForm();
            }
            currentForm.setFormId((Integer) formData.get("FormID"));
            System.out.println("DEBUG: Set currentForm.FormId = " + currentForm.getFormId());
            
            Object utdIdObj = formData.get("UtdID");
            String netId = (String) formData.get("NetID");
            studentIdField.setText(netId != null ? netId :
                (utdIdObj != null ? String.valueOf(utdIdObj) : ""));
            
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
                // Status 4 = Approved, check "ASSOCIATE DEAN RESPONSE"
                if ("4".equals(status)) {
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
        System.out.println("DEBUG: currentForm.getFormId()=" + (currentForm != null ? currentForm.getFormId() : "null"));
        
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
        
        if (currentForm != null && currentForm.getFormId() > 0) {
            TCEPDatabaseService.updateForm(currentForm.getFormId(), formData);
            System.out.println("Form updated successfully");
        } else if (currentForm != null && currentForm.getFormId() == 0) {
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
            currentForm.setFormId(newFormId);
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
        boolean isNewForm = (currentForm != null && currentForm.getFormId() == 0);

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
        if (currentForm == null || currentForm.getFormId() <= 0) {
            System.err.println("ERROR: Cannot delete - no form loaded or invalid FormID");
            return;
        }
        
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Form");
        confirmAlert.setHeaderText("Are you sure you want to delete this form?");
        confirmAlert.setContentText("Form ID: " + currentForm.getFormId() + "\nThis action cannot be undone.");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            TCEPDatabaseService.deleteForm(currentForm.getFormId());
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
}

 