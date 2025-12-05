package com.example.spotifyaisystem;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.nio.file.Path;
import java.util.List;

public class GUIController {
    @FXML private ToggleButton artistToggle;

    @FXML private TextField genreField;
    @FXML private TextField moodField;
    @FXML private TextField eraField;

    @FXML private Spinner<Integer> countSpinner;

    @FXML private Button exportButton;

    @FXML private TextArea outputArea;

    private final AiExplainer aiExplainer = new AiExplainer();

    private List<Track> recommendations;
    private List<Artist> artistRecommendations;

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
        String eraLine = eraField.getText().trim();

        if (!genreLine.isBlank() && !moodLine.isBlank() && !eraLine.isBlank()) {
            exportButton.setDisable(false);
            Preference preference = new Preference(genreLine, moodLine, eraLine);

            if (artistToggle.getText().equals("Artists")) {
                try {
                    recommendations = aiExplainer.getRecommendations(preference, countSpinner.getValue());
                    aiExplainer.scoreRecommendations(recommendations, preference);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < recommendations.size(); i++) {
                        String label = (i + 1) + " " + '"' + recommendations.get(i).getTitle() + '"' + " by " + recommendations.get(i).getArtistName() + "\n" +
                                "      Score: " + recommendations.get(i).getScore() + "\n" +
                                "      Link: " + recommendations.get(i).getLink() + "\n\n";

                        sb.append(label);
                    }

                    String aiexplanation = "AI Explanation: \n" + aiExplainer.explainRecommendations(preference, recommendations) + "\n";
                    sb.append(aiexplanation);

                    outputArea.setText(sb.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                try {
                    artistRecommendations = aiExplainer.getArtistRecommendations(preference, countSpinner.getValue());

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < recommendations.size(); i++) {
                        String label = (i + 1) + " " + '"' + artistRecommendations.get(i).getName() + '"' + " with " + artistRecommendations.get(i).getFollowers() + " followers\n" +
                                "      Popularity: " + artistRecommendations.get(i).getPopularity() + "\n" +
                                "      Genres: " + artistRecommendations.get(i).getGenres() + "\n" +
                                "      Link: " + artistRecommendations.get(i).getLink() + "\n\n";

                        sb.append(label);
                    }

                    String aiexplanation = "AI Explanation: \n" + aiExplainer.explainArtistRecommendations(preference, artistRecommendations) + "\n";
                    sb.append(aiexplanation);

                    outputArea.setText(sb.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }



        } else {
            outputArea.setText("Please enter at least one field.");
        }
    }

    public void export() {
        RecommendationExporter exporter = new RecommendationExporter();
        try {
            Path path;
            if (artistToggle.getText().equals("Songs")) {
                path = exporter.exportAsCsvArtists(artistRecommendations, "artists_gui.cvs");
            } else {
                path = exporter.exportAsCsv(recommendations, "recommendations_gui.cvs");
            }

            outputArea.appendText("\nExported to: " + path.toAbsolutePath() + "\n");

        } catch (Exception ex) {
            outputArea.appendText("\nFailed to export: " + ex.getMessage() + "\n");
        }
    }


    public void toggle() {
        String s = artistToggle.getText();

        if (s.equals("Artists")) {
            artistToggle.setText("Songs");
        } else {
            artistToggle.setText("Artists");
        }
    }
}
