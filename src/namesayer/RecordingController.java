package namesayer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class RecordingController {

    @FXML
    private Button OkButton;

    @FXML
    void CloseWindow(MouseEvent event) {

        // Closes the window that contains the NoButton.
        Stage stage = (Stage) OkButton.getScene().getWindow();
        stage.close();

    }

}