/***********************************************************************************************************************
 * Handles login/logout
 * Davis Huynh (dxh170005)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import utd.tcep.data.TCEPUser;
import utd.tcep.db.TCEPDatabaseService;

// Written by Davis Huynh (dxh170005)
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;

    private NavigationController navigationController;

    public void setNavigationController(NavigationController controller) {
        this.navigationController = controller;
    }

    // Handle login button click
    // Written by Davis Huynh (dxh170005)
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();

        loginMessage.setVisible(false);

        if (username.isEmpty()) {
            showError("Please enter your Advisor_Email.");
            loginMessage.setVisible(true);
            return;
        }

        if (userExists(username)) {
            try {
                Map<String, Object> advisor = TCEPDatabaseService.getAdvisorByEmail(username);
                if (advisor != null) {
                    TCEPUser.setCurrentUser(
                        username,
                        (Integer) advisor.get("AdvisorID"),
                        (String) advisor.get("Advisor_Name")
                    );
                    System.out.println("Logged in as: " + advisor.get("Advisor_Name") + " (ID: " + advisor.get("AdvisorID") + ")");
                } else {
                    TCEPUser.setCurrentUser(username, null, null);
                    System.out.println("Logged in as: " + username);
                }
            } catch (SQLException e) {
                System.err.println("Error loading advisor info: " + e.getMessage());
                TCEPUser.setCurrentUser(username, null, null);
            }
            
            navigationController.onLoginSuccess();
        } else {
            showError("User not found");
        }

    }

    // Check if user exists in the database
    // Written by Davis Huynh (dxh170005)
    private boolean userExists(String username) {
        try {
            return TCEPDatabaseService.userExists(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showError(String message) {
        loginMessage.setText(message);
        loginMessage.setVisible(true);
    }

    public void resetFields() {
        usernameField.clear();
        loginMessage.setVisible(false);
    }
}