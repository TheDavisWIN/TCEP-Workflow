/***********************************************************************************************************************
 * Handles login/logout
 * Davis Huynh (dxh170005)
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

        Integer advisorId = getAdvisorId(username);
        if (advisorId != null) {
            TCEPUser user = new TCEPUser(username, advisorId);
            System.out.println("Logged in as: " + user.getUsername() + " (AdvisorID: " + advisorId + ")");
            navigationController.onLoginSuccess(user);
        } else {
            showError("User not found");
        }

    }

    // Check if user exists in the database and return advisor ID
    // Written by Davis Huynh (dxh170005)
    private Integer getAdvisorId(String username) {
        String sql = "SELECT AdvisorID FROM advisor WHERE Advisor_Email = ?";
        try {
            Connection conn = TCEPDatabaseService.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("AdvisorID");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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