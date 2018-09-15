package namesayer;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;

import javax.swing.*;
import java.util.ArrayList;

public class MainMenuController implements Initializable {

    private Service<Void> backgroundThread;

    @FXML
    private Button CreateButton;

    @FXML
    private TextField TextField;

    @FXML
    private ObservableList<Creation> creationsList = FXCollections.observableArrayList();

    @FXML
    private ListView<Creation> creationsListView = new ListView<Creation>();

    @FXML
    private VBox vBox;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button PlayButton;

    @FXML
    private Button DeleteButton;

    private File creationsFile = new File("./Creations");

    private Creation newCreation;

    private MediaPlayer player;

    //This method creates a new creation when the Create button is clicked
    @FXML
    void CreateCreation(MouseEvent event) {
        String creationName = TextField.getText();
        Creation temp = new Creation();

        //Check if the creation name is in the valid format
        if (!temp.checkValidCreationName(creationName)) {
            //If incorrect, display error message
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Please only use letters (a-z), numbers," +
                            "\nunderscores and hyphens.", ButtonType.OK);
            error.setHeaderText("ERROR: Invalid Creation Name");
            error.setTitle("Invalid Creation Name");
            error.showAndWait();

            //Check if the creation already exists
        } else {
            //If the creation does not exist, create a new creation
            if (!temp.checkNameExists(creationsList, creationName)) {
                newCreation = new Creation(creationName);
                addCreationList(newCreation);

                //Record the user saying the creation name
                recordAudio(newCreation);

                //Overwrite the creation
            } else {
                overwriteCreation(creationName);
            }
        }
        //Reset the textbox to be empty
        TextField.clear();
    }

    //This function updates the list of creations
    private void addCreationList(Creation newCreation) {
        creationsList.add(newCreation);
        creationsListView.getItems().setAll(creationsList);
        creationsListView.setCellFactory(new CreationCellFactory());
    }

    private void overwriteCreation(String creationName) {

        //Display a pop up window asking the user to confirm whether they want to overwrite the creation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "There is already an existing creation\nwith the same name.", ButtonType.YES, ButtonType.CANCEL);
        alert.setHeaderText("Are you sure you want to overwrite " + creationName + "?");
        alert.setGraphic(null);
        alert.setTitle("Overwrite Creation");
        alert.showAndWait();

        //If they click yes, overwrite the creation
        if (alert.getResult() == ButtonType.YES) {

            //Delete the creation video file
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "rm " + creationName + ".mp4");

            try {
                Process p = builder.start();
                p.waitFor();

                int index = 0;
                for (Creation temp : creationsList) {
                    if (temp.getCreationName().equals(creationName)) {
                        index++;
                    }
                }
                Creation newCreation = creationsList.get(index);
                recordAudio(newCreation);

            } catch (IOException e) {

            } catch (InterruptedException e) {

            }
        }
    }

    //This method records audio of the user saying the creation name
    private void recordAudio(Creation creation) {

        //Popup a new window to alert the user to record the audio
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Press OK to start recording.", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle("Record Audio");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            try {
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -f alsa -ac 1 -ar 44100 -i default -t 00:00:05 audio.wav");
                builder.directory(creationsFile);
                Process audio = builder.start();
                Stage stage = PopupWindow("RecordingWindow.fxml");

                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(event -> {
                    stage.close();
                });
                delay.play();

                askRerecord(creation);

            } catch (IOException e) {
            }
        } else {
            //No creation is created if the user exits
            creationsList.remove(creation);
            creationsListView.getItems().setAll(creationsList);
            creationsListView.setCellFactory(new CreationCellFactory());
        }
    }

    //This method asks the user if they want to record their audio again
    private void askRerecord(Creation creation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to record your audio again?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle("Record Audio");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            recordAudio(creation);
        } else {
            createVideo(creation);
        }
    }

    //This method creates a video with the creatio name displayed
    private void createVideo(Creation creation) {

        try {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
                    "ffmpeg -f lavfi -i color=c=hotpink:s=320x240:d=5 -vf \"drawtext=/usr/share/fonts/truetype/liberation/\\\n" +
                            "LiberationSans-Regular.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text=" + creation.getCreationName() + "\" video.mp4");

            builder.directory(creationsFile);
            Process process = builder.start();
            process.waitFor();
            mergeVideo(creation);
        } catch (IOException | InterruptedException e) {

        }
    }

    //This method merges the .wav and .mp4 files into an video with an audio recording
    private void mergeVideo(Creation creation) {
        ProcessBuilder mergefile;
        mergefile = new ProcessBuilder("ffmpeg", "-y", "-i", "Creations/video.mp4", "-i", "Creations/audio.wav",
                "-c:v", "copy", "-c:a", "aac", "-strict", "experimental", "Creations/" + creation.getCreationName() + ".mp4");
        try {
            Process merge = mergefile.start();
            merge.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        ProcessBuilder removeVideo = new ProcessBuilder("rm", "Creations/video.mp4");
        ProcessBuilder removeAudio = new ProcessBuilder("rm", "Creations/audio.wav");
        try {
            Process video = removeVideo.start();
            video.waitFor();
            Process audio = removeAudio.start();
            audio.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //This method deletes a creation that has been selected when the Delete button is clicked
    @FXML
    void DeleteCreation(MouseEvent event) {

        //If there are no creations, no creations can be deleted
        if (creationsList.isEmpty()) {
            Alert error = new Alert(Alert.AlertType.ERROR, "There are no creations available to delete.", ButtonType.OK);
            error.setHeaderText("ERROR: Invalid Deletion");
            error.setTitle("Invalid Deletion");
            error.showAndWait();

        } else {
            Creation creationSelect = creationsListView.getSelectionModel().getSelectedItem();

            //If no creation is selected, display a message asking the user to select a creation
            if (creationsListView.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a creation to delete.", ButtonType.OK);
                alert.setHeaderText("No Creation Selected");
                alert.setTitle("Delete Creation");
                alert.showAndWait();

            } else {
                //Ask the user if they want to delete the creation again to confirm
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You will not be able to undo any changes.", ButtonType.YES, ButtonType.CANCEL);
                alert.setHeaderText("Are you sure you want to delete " + creationSelect.getCreationName() + "?");
                alert.setGraphic(null);
                alert.setTitle("Delete Creation");
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    //Remove the creation name form the list
                    creationsList.remove(creationSelect);
                    creationsListView.getItems().setAll(creationsList);
                    mediaView.setMediaPlayer(null);

                    try {
                        //Remove the creation video from the Creations folder
                        ProcessBuilder builder2 = new ProcessBuilder("/bin/bash", "-c", "rm Creations/" + creationSelect.getCreationName() + ".mp4");
                        builder2.start();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    //This method plays a creation when the Play button is clicked or pops up an error message
    @FXML
    void PlayCreation(MouseEvent event) {
        //If a creation video is already playing, stop and play the new video
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.stop();
        }

        //If there are no creations, no creations can be played
        if (creationsList.isEmpty()) {
            Alert error = new Alert(Alert.AlertType.ERROR, "There are no creations available to play.", ButtonType.OK);
            error.setHeaderText("ERROR: Invalid Playback");
            error.setTitle("Invalid Playback");
            error.showAndWait();

        } else {
            //This ensures that a creation is selected when asking to play
            if (creationsListView.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a creation to play.", ButtonType.OK);
                alert.setHeaderText("No Creation Selected");
                alert.setTitle("Play Creation");
                alert.showAndWait();
            } else {
                //Play creation
                Creation creationSelect = creationsListView.getSelectionModel().getSelectedItem();
                Media mediaPick = new Media(new File(System.getProperty("user.dir") + "/Creations/" + creationSelect).toURI().toString() + ".mp4");
                player = new MediaPlayer(mediaPick);
                mediaView.setMediaPlayer(player);
                player.play();

            }
        }
    }

    //This method creates a new pop up window
    private Stage PopupWindow(String resourceName) {

        Stage stage = new Stage();
        //This method pops up any windows
        try {

            Parent root = FXMLLoader.load(getClass().getResource(resourceName));
            stage.initStyle(StageStyle.UTILITY);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stage;
    }

    //This method reads the existing creation files into the system
    public void readFiles() {

        try {

            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "ls");
            builder.directory(creationsFile);
            Process process = builder.start();

            InputStream stdout = process.getInputStream();
            BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
            String line = null;

            while ((line = stdoutBuffered.readLine()) != null) {
                Creation c = new Creation(line);
                c.checkValidCreationName(line);
                creationsList.add(c);
                creationsListView.getItems().setAll(creationsList);
            }

            InputStream stderr = process.getErrorStream();
            BufferedReader stderrBuffered = new BufferedReader(new InputStreamReader(stderr));
            String error = null;

            if ((error = stderrBuffered.readLine()) != null) {
                Alert errorMessage = new Alert(Alert.AlertType.ERROR, null, ButtonType.OK);
                errorMessage.setHeaderText("ERROR: Invalid Playback");
                errorMessage.setTitle("Invalid Playback");
                errorMessage.showAndWait();
            }

            creationsListView.getItems().setAll(creationsList);
            stdoutBuffered.close();
            stderrBuffered.close();

        } catch (IOException error) {
        }
    }

    //This method will be called when the scene is created
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "mkdir ./Creations");
            builder.start();
            ProcessBuilder builder2 = new ProcessBuilder("/bin/bash", "-c", "rm Creations/audio.wav Creations/video.mp4");
            builder2.start();
            readFiles();

        } catch (IOException e) {
        }
    }
}
