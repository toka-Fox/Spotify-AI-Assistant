package com.example.spotifyaisystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

class AiExplainerTest {
    private AiExplainer aiExplainer = new AiExplainer();
    private String genre = "rock";
    private String mood = "happy";
    private String era = "90s";
    private Preference pref = new Preference(genre, mood, era);
    List<Track> tracks;

    @Test
    public void getRecommendationsTest() {
        tracks = aiExplainer.getRecommendations(pref, 2);

        for (Track track : tracks) {
            System.out.println(track);
        }
    }

    @Test
    public void explainRecommendationsTest() {
        tracks = aiExplainer.getRecommendations(pref, 2);
        String result = aiExplainer.explainRecommendations(pref, tracks);
        System.out.println(result);
    }

    @Test
    public void scoreRecommendationsTest() {
        tracks = aiExplainer.getRecommendations(pref, 2);
        aiExplainer.scoreRecommendations(tracks, pref);

        for (Track track : tracks) {
            System.out.println(track.getScore());
        }
    }
}
