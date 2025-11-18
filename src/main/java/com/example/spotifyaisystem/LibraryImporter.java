package com.example.spotifyaisystem;

import java.time.Instant;
import java.util.List;

public class LibraryImporter {

    public LibrarySnapshot importFromSpotify() {

        // 1) Try real Spotify API
        try {
            SpotifyApiClient client = new SpotifyApiClient();
            // you can tweak the query string if you want
            List<Track> fromSpotify = client.searchTracks("happy rock", 20);

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
