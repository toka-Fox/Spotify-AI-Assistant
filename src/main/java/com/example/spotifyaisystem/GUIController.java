package com.example.spotifyaisystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class GUIController {
    @FXML private TextField genreField;
    @FXML private TextField moodField;

    @FXML private Spinner<Integer> countSpinner;

    @FXML private Button recommendButton;
    @FXML private Button exportButton;

    @FXML private TextArea outputArea;

    private List<Track> library = List.of();
    private final Recommender recommender = new Recommender();
    private final AiExplainer aiExplainer = new AiExplainer();

    // remember last recommendations so we can export them
    private List<Recommendation> lastTopRecs = List.of();

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4);

        countSpinner.setValueFactory(valueFactory);
        countSpinner.setEditable(false);

        outputArea.setStyle("-fx-control-inner-background: rgb(108, 108, 115);" +
                "-fx-text-fill: white;" +
                "-fx-border-color: rgb(108, 108, 115);");
    }

    public void recommend() {
        String genreLine = genreField.getText().trim();
        String moodLine = moodField.getText().trim();
        boolean includeNewArtists = true;//newArtistsBox.isSelected();
        int requested = countSpinner.getValue();

        // Use only the first genre/mood for the Spotify query
        String genreForQuery = "";
        String moodForQuery = "";

        if (!genreLine.isBlank()) {
            genreForQuery = genreLine.split("\\s*,\\s*")[0];
        }
        if (!moodLine.isBlank()) {
            moodForQuery = moodLine.split("\\s*,\\s*")[0];
        }

        // Re-import the library from Spotify based on genre + mood
        LibrarySnapshot snapshot = aiExplainer.getRecommendations(genreForQuery, moodForQuery);
        library = snapshot.tracks();
        InputHandler handler = new InputHandler(library);

//        importedLabel.setText(
//                "Imported " + library.size() + " tracks at " + snapshot.importedAt()
//        );

        // Now parse full genre/mood lists for the preference object
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
        lastTopRecs = top;
        exportButton.setDisable(false);

        RecommendationSet topSet = new RecommendationSet();
        for (Recommendation r : top) {
            topSet.addRecommendation(r);
        }
        var links = handler.getLinks(topSet);

        StringBuilder sb = new StringBuilder();
        sb.append("Processed input: ").append(pi).append("\n\n");
        sb.append("Top ").append(n).append(" recommendations:\n");

        List<Track> recommendedTracks = new ArrayList<>();

        for (int i = 0; i < top.size(); i++) {
            Recommendation rec = top.get(i);
            Track track = findTrackById(library, rec.trackId());

            String label;
            if (track != null) {
                label = track.title() + " – " + track.artistName();
                recommendedTracks.add(track);
            } else {
                label = "Track ID " + rec.trackId();
            }

            sb.append(i + 1).append(". ").append(label).append("\n");
            sb.append("   Score: ").append(String.format("%.2f", rec.score())).append("\n");
            sb.append("   Link:  ").append(links.get(i).url()).append("\n\n");
        }

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
    }

    private Track findTrackById(List<Track> library, String id) {
        for (Track t : library) {
            if (t.id().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public void export() {
        if (lastTopRecs == null || lastTopRecs.isEmpty()) {
            outputArea.appendText("\nNo recommendations to export.\n");
            return;
        }
        RecommendationExporter exporter = new RecommendationExporter();
        try {
            var path = exporter.exportAsCsv(lastTopRecs, library, "recommendations_gui.txt");
            outputArea.appendText("\nExported to: " + path.toAbsolutePath() + "\n");
        } catch (Exception ex) {
            outputArea.appendText("\nFailed to export: " + ex.getMessage() + "\n");
        }
    }
}
