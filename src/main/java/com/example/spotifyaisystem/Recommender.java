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

    private double scoreTrack(Track track, ProcessedInput input) {
        String titleLower = track.title().toLowerCase();
        double score = 0.0;

        // genre matches from the title
        for (String g : input.genres()) {
            if (titleLower.contains(g.toLowerCase())) {
                score += 1.0;
            }
        }

        // mood matches from the title
        for (String m : input.moods()) {
            if (titleLower.contains(m.toLowerCase())) {
                score += 1.0;
            }
        }

        // new vs familiar artists preference
        boolean isFavoriteArtist = FAVORITE_ARTISTS.contains(track.artistName());
        if (input.includeNewArtists()) {
            // user wants new artists: bonus if it's NOT in the favorites list
            if (!isFavoriteArtist) {
                score += 0.5;
            }
        } else {
            // user prefers familiar artists: bonus if it IS in the favorites list
            if (isFavoriteArtist) {
                score += 0.5;
            }
        }

        return score;
    }
}
