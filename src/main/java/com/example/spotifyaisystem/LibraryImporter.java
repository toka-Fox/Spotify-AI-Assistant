package com.example.spotifyaisystem;

import java.time.Instant;
import java.util.List;

public class LibraryImporter {

    public LibrarySnapshot importFromSpotify(String genre, String mood) {

        String query = buildQuery(genre, mood);
        System.out.println("Spotify search query: " + query);

        // 1) Try real Spotify API
        try {
            SpotifyApiClient client = new SpotifyApiClient();
            List<Track> fromSpotify = client.searchTracks(query, 20);

            if (!fromSpotify.isEmpty()) {
                System.out.println("Loaded " + fromSpotify.size() + " tracks from Spotify API.");
                return new LibrarySnapshot(fromSpotify, Instant.now());
            } else {
                System.out.println("Spotify returned no tracks, falling back to local sample library.");
            }
        } catch (Exception e) {
            System.out.println("Spotify API failed: " + e.getMessage());
            System.out.println("Using local sample library instead.");
        }

        // 2) Fallback: your existing hard-coded sample library
        List<Track> fallback = buildFallbackTracks();
        return new LibrarySnapshot(fallback, Instant.now());
    }

    /**
     * Build a Spotify search query based on genre + mood.
     * Handles the four main combos explicitly:
     *  - rock + happy
     *  - rock + sad
     *  - pop + happy
     *  - pop + sad
     * and falls back to simple generic queries otherwise.
     */
    private String buildQuery(String genre, String mood) {
        String g = genre == null ? "" : genre.trim().toLowerCase();
        String m = mood == null ? "" : mood.trim().toLowerCase();

        // Four explicit combos
        if (g.equals("rock") && m.equals("happy")) return "happy rock";
        if (g.equals("rock") && m.equals("sad"))   return "sad rock";
        if (g.equals("pop")  && m.equals("happy")) return "happy pop";
        if (g.equals("pop")  && m.equals("sad"))   return "sad pop";

        // Generic fallbacks
        if (!g.isBlank() && !m.isBlank()) {
            return m + " " + g; // e.g. "chill rock", "angry metal"
        }
        if (!g.isBlank()) {
            return g + " music"; // e.g. "jazz music"
        }
        if (!m.isBlank()) {
            return m + " music"; // e.g. "sad music"
        }

        // No input at all – just grab something general
        return "popular music";
    }

    private List<Track> buildFallbackTracks() {
        Track t1 = new Track("1", "Hello World (Happy Rock Version)", "Artist A");
        Track t2 = new Track("2", "Goodbye (Sad Pop Ballad)", "Artist B");
        Track t3 = new Track("3", "Happy Rock Song", "Artist C");
        Track t4 = new Track("4", "Chill Pop Nights", "Artist D");
        Track t5 = new Track("5", "Sad Rock Ballad", "Artist E");
        Track t6 = new Track("6", "Energetic Rock Anthem", "Artist F");
        Track t7 = new Track("7", "Calm Piano Dreams", "Artist G");
        Track t8 = new Track("8", "Happy Pop Wave", "Artist H");
        Track t9 = new Track("9", "Dark Metal Storm", "Artist I");
        Track t10 = new Track("10", "Lo-Fi Chill Beats", "Artist J");
        Track t11 = new Track("11", "Summer Vibes (Pop)", "Artist K");
        Track t12 = new Track("12", "Emotional Sad Strings", "Artist L");

        return List.of(
                t1, t2, t3, t4, t5, t6,
                t7, t8, t9, t10, t11, t12
        );
    }
}
