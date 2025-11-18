package com.example.spotifyaisystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GuiApp extends Application {

    private List<Track> library;
    private InputHandler handler;
    private Recommender recommender;
    private AiExplainer aiExplainer;          // <-- NEW
    // If you want direct access to SpotifyApiClient in the GUI:
    // private SpotifyApiClient spotifyApiClient;

    // remember last recommendations so we can export them
    private List<Recommendation> lastTopRecs = List.of();

    @Override
    public void start(Stage stage) {
        // 1) Import library
        LibraryImporter importer = new LibraryImporter();
        LibrarySnapshot snapshot = importer.importFromSpotify();
        library = snapshot.tracks();
        handler = new InputHandler(library);
        recommender = new Recommender();

        // Initialize AiExplainer (OpenAI) once and reuse
        aiExplainer = new AiExplainer();

        // If you want to use SpotifyApiClient directly (optional):
        // spotifyApiClient = new SpotifyApiClient();

        // ======= UI controls =======
        TextField genreField = new TextField();
        genreField.setPromptText("rock,pop");

        TextField moodField = new TextField();
        moodField.setPromptText("happy,sad");

        CheckBox newArtistsBox = new CheckBox("Discover new artists");
        newArtistsBox.setSelected(true);

        Spinner<Integer> countSpinner = new Spinner<>(1, 5, 3);
        countSpinner.setEditable(false);

        Button recommendButton = new Button("Get Recommendations");
        Button exportButton = new Button("Export CSV");
        exportButton.setDisable(true); // disabled until we have results

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        Label importedLabel = new Label(
                "Imported " + library.size() + " tracks at " + snapshot.importedAt()
        );

        // Layout
        HBox genresRow = new HBox(10, new Label("Genres:"), genreField);
        HBox moodsRow = new HBox(10, new Label("Moods:"), moodField);
        HBox countRow = new HBox(10, new Label("How many results:"), countSpinner);
        HBox buttonsRow = new HBox(10, recommendButton, exportButton);

        VBox controls = new VBox(10,
                importedLabel,
                genresRow,
                moodsRow,
                newArtistsBox,
                countRow,
                buttonsRow
        );
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, controls, new Label("Recommendations + AI Explanation:"), outputArea);
        root.setPadding(new Insets(10));

        // ======= Button actions =======

        recommendButton.setOnAction(e -> {
            String genreLine = genreField.getText().trim();
            String moodLine = moodField.getText().trim();
            boolean includeNewArtists = newArtistsBox.isSelected();
            int requested = countSpinner.getValue();

            List<String> genres = genreLine.isBlank()
                    ? List.of()
                    : List.of(genreLine.split("\\s*,\\s*"));

            List<String> moods = moodLine.isBlank()
                    ? List.of()
                    : List.of(moodLine.split("\\s*,\\s*"));

            if (genres.isEmpty() && moods.isEmpty()) {
                outputArea.setText("Please enter at least one genre or mood.");
                exportButton.setDisable(true);
                lastTopRecs = List.of();
                return;
            }

            Preference pref = new Preference(genres, moods, includeNewArtists);
            ProcessedInput pi = handler.processInput(pref);

            RecommendationSet set = recommender.recommend(pi, library);
            if (set.size() == 0) {
                outputArea.setText("No recommendations matched your preferences.");
                exportButton.setDisable(true);
                lastTopRecs = List.of();
                return;
            }

            int n = Math.min(requested, set.size());
            List<Recommendation> top = set.getTopN(n);
            lastTopRecs = top;             // save for export
            exportButton.setDisable(false); // now we can export

            RecommendationSet topSet = new RecommendationSet();
            for (Recommendation r : top) {
                topSet.addRecommendation(r);
            }
            var links = handler.getLinks(topSet);

            StringBuilder sb = new StringBuilder();
            sb.append("Processed input: ").append(pi).append("\n\n");
            sb.append("Top ").append(n).append(" recommendations:\n");

            // Build list of concrete Track objects for the AI explainer
            List<Track> recommendedTracks = new ArrayList<>();

            for (int i = 0; i < top.size(); i++) {
                Recommendation rec = top.get(i);
                Track track = findTrackById(library, rec.trackId());

                String label;
                if (track != null) {
                    label = track.title() + " – " + track.artistName();
                    recommendedTracks.add(track);  // only add if we resolved the track
                } else {
                    label = "Track ID " + rec.trackId();
                }

                sb.append(i + 1).append(". ").append(label).append("\n");
                sb.append("   Score: ").append(String.format("%.2f", rec.score())).append("\n");
                sb.append("   Link:  ").append(links.get(i).url()).append("\n\n");
            }

            // ======= Ask OpenAI to explain the recommendations =======
            try {
                if (!recommendedTracks.isEmpty()) {
                    String explanation = aiExplainer.explainRecommendations(pi, recommendedTracks);
                    sb.append("AI explanation:\n");
                    sb.append(explanation).append("\n");
                } else {
                    sb.append("AI explanation unavailable (could not resolve track details).\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                sb.append("AI explanation failed: ")
                        .append(ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                        .append("\n");
            }

            outputArea.setText(sb.toString());
        });

        exportButton.setOnAction(e -> {
            if (lastTopRecs == null || lastTopRecs.isEmpty()) {
                outputArea.appendText("\nNo recommendations to export.\n");
                return;
            }
            RecommendationExporter exporter = new RecommendationExporter();
            try {
                var path = exporter.exportAsCsv(lastTopRecs, library, "recommendations_gui.csv");
                outputArea.appendText("\nExported to: " + path.toAbsolutePath() + "\n");
            } catch (Exception ex) {
                outputArea.appendText("\nFailed to export: " + ex.getMessage() + "\n");
            }
        });

        // ======= Show window =======
        Scene scene = new Scene(root, 700, 500);
        stage.setTitle("Spotify AI - JavaFX Demo");
        stage.setScene(scene);
        stage.show();
    }

    private Track findTrackById(List<Track> library, String id) {
        for (Track t : library) {
            if (t.id().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
