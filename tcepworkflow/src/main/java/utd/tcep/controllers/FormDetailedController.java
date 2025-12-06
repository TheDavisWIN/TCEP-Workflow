/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
 * Written by Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.File;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.controlsfx.control.CheckComboBox;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.data.TCEPForm;
import utd.tcep.data.TCEPUser;
import utd.tcep.main.TCEPWorkflowApp;
import utd.tcep.db.TCEPDatabaseService;

public class FormDetailedController {

    private TCEPForm currentForm;
    private String firstName;
    private String lastName;
    private String middleName;
    private boolean loadingForm = false;
    // stored department name for current student (set when overlay loads)
    private String currentStudentDepartment = null;
    // Map from displayed recipient name to AdvisorID (absent for department label)
    private final Map<String, Integer> recipientNameToId = new HashMap<>();
    // Reference to the form table controller to remove forms after actions
    private FormTableController formTableController;
    // Reference to the navigation controller to switch views
    private NavigationController navigationController;
    // Current logged-in user
    private TCEPUser currentUser;

    @FXML private Label startedDateLabel;
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
    @FXML private Button generatePdfButton;
    @FXML private AnchorPane overlayContainer;
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
    @FXML private ImageView previewImageView;
    @FXML private Button exportButton;
    @FXML private Button cancelButton;

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
    }
    // Show form's change history
    @FXML
    private void handleViewHistory() throws IOException {
        
    }

    private Connection connection;

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
        overlayContainer.setVisible(true);

        // Use logged-in advisor ID to exclude from recipient list
        Integer loggedInAdvisorId = (currentUser != null) ? currentUser.getAdvisorId() : null;
        
        // Get student's department for the department label
        String studentDeptName = null;
        try {
            if (currentForm != null && currentForm.getUtdId() != null && !currentForm.getUtdId().isEmpty()) {
                int studentId = Integer.parseInt(currentForm.getUtdId());
                String deptSql = "SELECT DepartmentID FROM Student WHERE StudentID = ?";
                try (PreparedStatement astmt = connection.prepareStatement(deptSql)) {
                    astmt.setInt(1, studentId);
                    ResultSet ars = astmt.executeQuery();
                    if (ars.next()) {
                        int depId = ars.getInt("DepartmentID");
                        if (!ars.wasNull()) {
                            try (PreparedStatement depStmt = connection.prepareStatement("SELECT Department_Name FROM Department WHERE DepartmentID = ?")) {
                                depStmt.setInt(1, depId);
                                ResultSet deps = depStmt.executeQuery();
                                if (deps.next()) studentDeptName = deps.getString("Department_Name");
                            }
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
                if (loggedInAdvisorId != null) stmt.setInt(1, loggedInAdvisorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("Advisor_Name");
                    int aid = rs.getInt("AdvisorID");
                    recipients.add(name);
                    recipientNameToId.put(name, aid);
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
                if (loggedInAdvisorId != null) stmt.setInt(1, loggedInAdvisorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("Advisor_Name");
                    int aid = rs.getInt("AdvisorID");
                    recipients.add(name);
                    recipientNameToId.put(name, aid);
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
                if (loggedInAdvisorId != null) stmt.setInt(1, loggedInAdvisorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("Advisor_Name");
                    int aid = rs.getInt("AdvisorID");
                    recipients.add(name);
                    recipientNameToId.put(name, aid);
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
    
    // Changes made to approval actio   n
    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmApproval() throws IOException {
        String reason = approvalReasonCombo == null ? null : approvalReasonCombo.getValue();
        if ("Other".equals(reason) && approvalReasonOtherField != null) {
            reason = approvalReasonOtherField.getText();
        }

        // Read checked recipients from the CheckComboBox
        java.util.List<String> recipients = approvalRecipientCombo == null ? java.util.Collections.emptyList() : approvalRecipientCombo.getCheckModel().getCheckedItems();
        String recipient = String.join(", ", recipients);
        System.out.println("Approval confirmed. Reason: " + reason + ", Recipient: " + recipient);

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        // persist a status change row for each selected recipient
        processRecipientsForAction(recipients, "APPROVED", reason);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formapprovalview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        loader.load();

        closeOverlay();

        // Show PDF preview after closing overlay if department is selected
        if (showingPDF) {
            String templatePath = "Blank TCEP.pdf";
            boolean pdfExported = showPDFPreview(templatePath);
            // Only remove form if PDF was actually exported (not canceled)
            if (pdfExported && formTableController != null && currentForm != null) {
                System.out.println("Removing form " + currentForm.getId() + " from table (after PDF exported)");
                formTableController.removeFormFromUI(currentForm);
            }
            // Navigate back to form list after PDF dialog is closed
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        } else {
            // Navigate back to form list immediately if no PDF
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        }
    }

    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmDenial() throws IOException {
        String reason = denialReasonCombo == null ? null : denialReasonCombo.getValue();
        if ("Other".equals(reason) && denialReasonOtherField != null) {
            reason = denialReasonOtherField.getText();
        }

        // Read checked recipients from the CheckComboBox
        java.util.List<String> recipients = denialRecipientCombo == null ? java.util.Collections.emptyList() : denialRecipientCombo.getCheckModel().getCheckedItems();
        String recipient = String.join(", ", recipients);
        System.out.println("Denial confirmed. Reason: " + reason + ", Recipient: " + recipient);

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        // persist a status change row for each selected recipient
        processRecipientsForAction(recipients, "DENIED", reason);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
        }

        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formdenialview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        loader.load();

        closeOverlay();

        // Show PDF preview after closing overlay if department is selected
        if (showingPDF) {
            String templatePath = "Blank TCEP.pdf";
            boolean pdfExported = showPDFPreview(templatePath);
            // Only remove form if PDF was actually exported (not canceled)
            if (pdfExported && formTableController != null && currentForm != null) {
                System.out.println("Removing form " + currentForm.getId() + " from table (after PDF exported)");
                formTableController.removeFormFromUI(currentForm);
            }
            // Navigate back to form list after PDF dialog is closed
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        } else {
            // Navigate back to form list immediately if no PDF
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        }
    }

    // Changes made to send back action
    // Written by Nicolas Hartono (nxh210004)
    @FXML
    public void confirmSendBack() throws IOException {
        String reason = sendBackReasonCombo == null ? null : sendBackReasonCombo.getValue();
        if ("Other".equals(reason) && sendBackReasonOtherField != null) {
            reason = sendBackReasonOtherField.getText();
        }

        // Read checked recipients from the CheckComboBox
        java.util.List<String> recipients = sendBackRecipientCombo == null ? java.util.Collections.emptyList() : sendBackRecipientCombo.getCheckModel().getCheckedItems();
        String recipient = String.join(", ", recipients);
        System.out.println("Send back confirmed. Reason: " + reason + ", Recipient: " + recipient);

        String deptLabel = (currentStudentDepartment != null && !currentStudentDepartment.isEmpty()) ? currentStudentDepartment : "CS Department";
        boolean showingPDF = recipients.contains(deptLabel);

        // persist a status change row for each selected recipient
        processRecipientsForAction(recipients, "SENT_BACK", reason);

        // Remove form from table if NOT showing PDF (defer removal until after PDF is created)
        if (!showingPDF && formTableController != null && currentForm != null) {
            System.out.println("Removing form " + currentForm.getId() + " from table (no PDF)");
            formTableController.removeFormFromUI(currentForm);
        }
        
        FXMLLoader loader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/formsendbackview.fxml"));

        // Use this controller for overlay callbacks (so overlay can call closeOverlay())
        loader.setController(this);
        loader.load();
        closeOverlay();

        // Show PDF preview after closing overlay if department is selected
        if (showingPDF) {
            String templatePath = "Blank TCEP.pdf";
            boolean pdfExported = showPDFPreview(templatePath);
            // Only remove form if PDF was actually exported (not canceled)
            if (pdfExported && formTableController != null && currentForm != null) {
                System.out.println("Removing form " + currentForm.getId() + " from table (after PDF exported)");
                formTableController.removeFormFromUI(currentForm);
            }
            // Navigate back to form list after PDF dialog is closed
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        } else {
            // Navigate back to form list immediately if no PDF
            if (navigationController != null) {
                navigationController.swapView(NavigationController.View.Table);
            }
        }
    }

    @FXML
    public void handleGeneratePDF(String templatePath, String outputPath) {
        exportPDF(templatePath, outputPath);
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
                fileChooser.setInitialFileName("TCEP_Form.pdf");
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
            return false;
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
    }

    // Set currently managed form and fill all text fields in UI when set
    // Written by Ryan Pham (rkp200003)
    public void setForm(TCEPForm form) {
        loadingForm = true; // set loadingForm to true to prevent listeners from firing while populating fields
        currentForm = form;

        clearForm();

        // Populate name fields by splitting full name
        if (form.getStudentName() != null)
        {
            String[] nameSplit = form.getStudentName().split(" ");

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
        
        if (form.getStartedDate() != null) {
            startedDateLabel.setText("Started on: " + form.getStartedDate().toString());
        } else {
            startedDateLabel.setText("ERROR: No start date");
        }
        

        loadByFormID(form.getId());
        loadingForm = false;
    }

    // Written by Ryan Pham (rkp200003)
    public TCEPForm getForm() {
        return currentForm;
    }

    // Set the form table controller reference to allow removing forms
    public void setFormTableController(FormTableController controller) {
        this.formTableController = controller;
    }

    // Set the navigation controller reference to allow switching views
    public void setNavigationController(NavigationController controller) {
        this.navigationController = controller;
    }

    // Set the current logged-in user
    public void setCurrentUser(TCEPUser user) {
        this.currentUser = user;
    }

    // Load form data from database by ID (NetID or UTDID) and populate fields
    // Written by Davis Huynh (dxh170005)
    public void loadByFormID(int formId) {
        String sql = "SELECT f.StudentID, s.Student_Name, f.Degree_Requirement, f.Core_Designation, f.NetID, s.UtdID, "
                + "f.Incoming_CourseID, ic.CourseName AS IncomingCourseName, ic.CourseNumber AS IncomingCourseNumber,"
                + "f.Equivalent_CourseID, ec.CourseName AS EquivalentCourseName, ec.CourseNumber AS EquivalentCourseNumber, "
                + "f.InstitutionID, inst.Institution_Name AS InstitutionName "
                + "FROM tcep_form f "
                + "LEFT JOIN student s ON f.StudentID = s.StudentID "
                + "LEFT JOIN incoming_course ic ON f.Incoming_CourseID = ic.Incoming_CourseID "
                + "LEFT JOIN equivalent_course ec ON f.Equivalent_CourseID = ec.Equivalent_CourseID "
                + "LEFT JOIN institution inst ON f.InstitutionID = inst.InstitutionID "
                + "WHERE f.FormID = ?";
        
        try {
            Connection conn = TCEPDatabaseService.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, Integer.toString(formId));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Found matching record in database");
                studentIdField.setText(rs.getString("NetID") != null ? rs.getString("NetID") :
                    (rs.getString("UtdID") != null ? rs.getString("UtdID") : ""));
                origCourseNumField.setText(rs.getString("IncomingCourseNumber") != null ? rs.getString("IncomingCourseNumber") : "");
                origCourseTitleField.setText(rs.getString("IncomingCourseName") != null ? rs.getString("IncomingCourseName") : "");
                sourceInstitutionNameField.setText(rs.getString("InstitutionName") != null ? rs.getString("InstitutionName") : "");
                equivalentCourseField.setText(rs.getString("EquivalentCourseNumber") != null ? rs.getString("EquivalentCourseNumber") : "");
                satisfiedRequirementField.setText(rs.getString("Degree_Requirement") != null ? rs.getString("Degree_Requirement") : "");
                coreDesignationField.setText(rs.getString("Core_Designation") != null ? rs.getString("Core_Designation") : "");
                // System.out.println("Fields populated successfully");
            } else {
                System.out.println("No matching record found for id: " + formId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            // fillField(acroForm, "coreDesignation", coreDesignationField.getText());

            // Optional: Make fields read-only
            acroForm.flatten();

            pdfDoc.save(outputPath);
            pdfDoc.close();

            System.out.println("PDF exported successfully to: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Process each selected recipient and write a status/history row via the DB service.
    private void processRecipientsForAction(java.util.List<String> recipients, String actionType, String comments) {
        if (recipients == null || recipients.isEmpty() || currentForm == null) {
            System.out.println("processRecipientsForAction: No recipients or no current form");
            return;
        }
        int formId = currentForm.getId();
        System.out.println("Processing " + recipients.size() + " recipients for action: " + actionType);
        for (String recipient : recipients) {
            Integer advisorId = recipientNameToId.get(recipient);
            String departmentName = null;
            if (advisorId == null) {
                // treat as department-level recipient
                departmentName = recipient;
            }
            TCEPDatabaseService.addStatusChange(formId, actionType, comments, advisorId, departmentName);
        }
        // Update form status in database (2=Approved, 3=Denied, 1=Pending/Sent Back)
        int newStatusId = actionType.equals("APPROVED") ? 2 : (actionType.equals("DENIED") ? 3 : 1);
        TCEPDatabaseService.updateFormStatus(formId, newStatusId);
    }

    // Concatenate first, middle, last names since DB only has one row for name
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
}
 
