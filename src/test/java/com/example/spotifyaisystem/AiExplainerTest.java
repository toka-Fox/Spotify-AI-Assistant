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

    @BeforeEach
    public void setUp() {
        ai = new AiExplainer();
        LibrarySnapshot result = ai.getRecommendations(genre, mood);
        recommendedTracks = result.tracks();
    }

    @Test
    public void getRecommendationsTest() {
        for (Track song : recommendedTracks) {
            System.out.println(song);
        }
    }

    @Test
    public void explainRecommendationsTest() {
        InputHandler handler = new InputHandler(recommendedTracks);
        Preference pref = new Preference(
                Collections.singletonList(genre),
                Collections.singletonList(mood),
                true
        );
        ProcessedInput input = handler.processInput(pref);

        String result = ai.explainRecommendations(input, recommendedTracks);
        System.out.println(result);
    }
}
