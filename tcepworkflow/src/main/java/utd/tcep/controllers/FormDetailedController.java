/***********************************************************************************************************************
 * JavaFX Controller for detailed interaction with fields in a TCEP form
 * Written by Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
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

        clearForm();

        if (form.getStudentName() != null)
        {
            String[] nameSplit = form.getStudentName().split(" ");

            firstNameField.setText(nameSplit[0]);
            lastNameField.setText(nameSplit[1]);
        }

        // origCourseNumField.setText(form.orig);
        // origCourseTitleField.setText();
        // origCreditHoursField.setText();
        // sourceInstitutionNameField.setText();
        // sourceInstitutionLocationField.setText();
        // equivalentCourseField.setText();
        // satisfiedRequirementField.setText();
        // coreDesignationField.setText();
    }

    // Written by Ryan Pham (rkp200003)
    public TCEPForm getForm() {
        return currentForm;
    }
}
 