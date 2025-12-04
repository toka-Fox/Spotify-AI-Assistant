package com.example.spotifyaisystem;

import java.util.ArrayList;
import java.util.List;

public class Recommender {

    // Pretend these are "familiar" or favorite artists for the user
    private static final List<String> FAVORITE_ARTISTS = List.of("Artist A", "Artist B");

    public RecommendationSet recommend(ProcessedInput input, List<Track> library) {
        List<Recommendation> scored = new ArrayList<>();

        // 1) Score each track (keep all of them)
        for (Track t : library) {
            double score = scoreTrack(t, input);
            // previously we only added if score > 0.0
            scored.add(new Recommendation(0, score, t.id()));
        }

        // if somehow the library is empty, just return an empty set
        if (scored.isEmpty()) {
            return new RecommendationSet();
        }

        // 2) Sort by score (highest first)
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));

        // 3) Turn scores into ranked recommendations (1 = best)
        RecommendationSet set = new RecommendationSet();
        int rank = 1;
        for (Recommendation r : scored) {
            set.addRecommendation(new Recommendation(rank++, r.score(), r.trackId()));
        }

        return set;
    }

    private int scoreTrack(Track track, ProcessedInput input) {
        AiExplainer ai = new AiExplainer();

        String result = ai.scoreRecommendations(track, input);
        System.out.println(result);
        int score = Integer.parseInt(result);

        return score;
    }
}
