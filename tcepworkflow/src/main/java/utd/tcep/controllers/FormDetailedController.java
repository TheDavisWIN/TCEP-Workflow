package utd.tcep.controllers;

import java.io.IOException;
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
// removed unused imports
import utd.tcep.data.TCEPForm;
import utd.tcep.main.TCEPWorkflowApp;

public class FormDetailedController {
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

    static public TCEPForm currentForm;
    private NavigationController navigationController;  // ← injected

    public void setNavigationController(NavigationController nav) {
        this.navigationController = nav;
    }

    @FXML
    public void handleViewHistory() throws IOException {
        if (navigationController != null) {
            navigationController.swapView("/utd/tcep/formhistoryview");
        }
    }

    @FXML
    public void handleBackToForm() throws IOException {
        if (navigationController != null) {
            navigationController.swapView("/utd/tcep/formdetailedview");
        }
    }

    // Load an overlay FXML into the overlay container and make it visible.
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
    public void closeOverlay() {
        overlayContainer.getChildren().clear();
        overlayContainer.setVisible(false);
    }

    @FXML
    public void handleAccept() throws IOException {
        // load approval overlay instead of using alerts/popups
        loadOverlay("/utd/tcep/formapprovalview");
    }

    @FXML
    public void handleDeny() throws IOException {
        loadOverlay("/utd/tcep/formdenialview");
    }

    @FXML
    public void handleSendBack() throws IOException {
        loadOverlay("/utd/tcep/formsendbackview");
    }

    @FXML
    public void confirmApproval() throws IOException {
        String reason = approvalReasonCombo == null ? null : approvalReasonCombo.getValue();
        if ("Other".equals(reason) && approvalReasonOtherField != null) {
            reason = approvalReasonOtherField.getText();
        }
        String recipient = approvalRecipientCombo == null ? null : approvalRecipientCombo.getValue();
        System.out.println("Approval confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: persist approval action or update model here
        if (navigationController != null) {
            navigationController.swapView("/utd/tcep/formapprovalview");
        }
        closeOverlay();
    }

    @FXML
    public void confirmDenial() throws IOException {
        String reason = denialReasonCombo == null ? null : denialReasonCombo.getValue();
        if ("Other".equals(reason) && denialReasonOtherField != null) {
            reason = denialReasonOtherField.getText();
        }
        String recipient = denialRecipientCombo == null ? null : denialRecipientCombo.getValue();
        System.out.println("Denial confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: persist denial action or update model here
        if (navigationController != null) {
            navigationController.swapView("/utd/tcep/formdenialview");
        }
        closeOverlay();
    }

    @FXML
    public void confirmSendBack() throws IOException {
        String reason = sendBackReasonCombo == null ? null : sendBackReasonCombo.getValue();
        if ("Other".equals(reason) && sendBackReasonOtherField != null) {
            reason = sendBackReasonOtherField.getText();
        }
        String recipient = sendBackRecipientCombo == null ? null : sendBackRecipientCombo.getValue();
        System.out.println("Send back confirmed. Reason: " + reason + ", Recipient: " + recipient);

        // TODO: perform any DB updates or messaging here using reason/recipient

        if (navigationController != null) {
            navigationController.swapView("/utd/tcep/formsendbackview");
        }
        closeOverlay();
    }

    public void loadForm() { }
}