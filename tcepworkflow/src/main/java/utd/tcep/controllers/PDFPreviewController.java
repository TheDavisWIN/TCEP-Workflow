package utd.tcep.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PDFPreviewController {
    
    @FXML private ImageView previewImageView;
    @FXML private Button exportButton;
    @FXML private Button cancelButton;

    public ImageView getPreviewImageView() {
        return previewImageView;
    }

    public Button getExportButton() {
        return exportButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void setPreviewImage(Image image) {
        previewImageView.setImage(image);
    }
}
