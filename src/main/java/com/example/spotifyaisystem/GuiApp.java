package com.example.spotifyaisystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class GuiApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spotifyaisystem/gui.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setTitle("Spotify AI - JavaFX Demo");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("what the fuck");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
