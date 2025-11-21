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
import javafx.scene.image.Image;
import utd.tcep.controllers.NavigationController;

public class TCEPWorkflowApp extends Application {

    private Scene scene;
    private NavigationController navigationController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TCEPWorkflowApp.class.getResource("/utd/tcep/main.fxml"));
        scene = new Scene(fxmlLoader.load(), 1600, 900);
        stage.setScene(scene);
        stage.getIcons().add(new Image(TCEPWorkflowApp.class.getResourceAsStream("/utd/tcep/images/utdcircle2.png")));
        stage.show();

        navigationController = fxmlLoader.getController();
        navigationController.swapView(NavigationController.View.Login);
    }

    public static void main(String[] args) {
        launch();
    }

}