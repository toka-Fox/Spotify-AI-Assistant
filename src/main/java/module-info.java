module com.example.spotifyaisystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.spotifyaisystem to javafx.fxml;
    exports com.example.spotifyaisystem;
}