package com.example.spotifyaisystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

class AiExplainerTest {
    private AiExplainer ai;
    private List<Track> recommendedTracks;
    private final String genre = "Classic Rock";
    private final String mood = "Exciting";
    private final String era = "90s";

    ProcessedInput input;

    @BeforeEach
    public void setUp() {
        ai = new AiExplainer();
        LibrarySnapshot result = ai.getRecommendations(genre, mood, era, 2);
        recommendedTracks = result.tracks();

        InputHandler handler = new InputHandler(recommendedTracks);
        Preference pref = new Preference(
                Collections.singletonList(genre),
                Collections.singletonList(mood),
                Collections.singletonList(era),
                true
        );
        input = handler.processInput(pref);
    }

    @Test
    public void getRecommendationsTest() {
        for (Track song : recommendedTracks) {
            System.out.println(song);
        }
    }

    @Test
    public void explainRecommendationsTest() {
        String result = ai.explainRecommendations(input, recommendedTracks);
        System.out.println(result);
    }

    @Test
    public void scoreRecommendationsTest() {
        for (Track track : recommendedTracks) {
            int score = Integer.parseInt(ai.scoreRecommendations(track, input));
            System.out.println(score);
        }
    }
}
