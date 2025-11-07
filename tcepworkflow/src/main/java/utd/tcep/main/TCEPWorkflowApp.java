/***********************************************************************************************************************
 * Starting point for the program
 * Ryan Pham
***********************************************************************************************************************/

package utd.tcep.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utd.tcep.controllers.NavigationController;

public class TCEPWorkflowApp extends Application {

    private Scene scene;
    private NavigationController navigationController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/main.fxml"));
        scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setScene(scene);
        stage.show();

        navigationController = fxmlLoader.getController();
        navigationController.swapView(NavigationController.View.Table);
    }

    public static void main(String[] args) {
        launch();
    }

}