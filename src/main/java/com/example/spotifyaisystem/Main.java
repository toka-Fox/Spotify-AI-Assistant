package com.example.spotifyaisystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // ===============================
        //  Spotify AI - Recommendation Demo
        // ===============================
        System.out.println("======================================");
        System.out.println("   Spotify AI - Recommendation Demo   ");
        System.out.println("======================================\n");

        // 1) Import music library (Phase 2: Import Music Library)
        LibraryImporter importer = new LibraryImporter();
        LibrarySnapshot snapshot = importer.importFromSpotify();
        List<Track> library = snapshot.tracks();

        System.out.println("Imported " + library.size() + " tracks at " + snapshot.importedAt());
        System.out.println();

        // 2) Create the handler with this library
        InputHandler handler = new InputHandler(library);

        // 3) Ask the user for their preferences
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your favorite genres (comma separated, e.g., rock,pop): ");
        String genreLine = scanner.nextLine();
        List<String> genres = genreLine.isBlank()
                ? List.of()
                : List.of(genreLine.split("\\s*,\\s*"));

        System.out.print("Enter your moods (comma separated, e.g., happy,sad): ");
        String moodLine = scanner.nextLine();
        List<String> moods = moodLine.isBlank()
                ? List.of()
                : List.of(moodLine.split("\\s*,\\s*"));

        System.out.print("Do you want to discover NEW artists? (yes/no): ");
        String includeLine = scanner.nextLine().trim().toLowerCase();
        boolean includeNewArtists = includeLine.isBlank() || includeLine.startsWith("y");

        if (genres.isEmpty() && moods.isEmpty()) {
            System.out.println("\nYou didn't enter any genres or moods, so I can't recommend anything.");
            System.out.println("Done. Process finished.");
            return;
        }

        System.out.print("How many recommendations would you like (1–5)? ");
        String nLine = scanner.nextLine().trim();
        int requested;
        try {
            requested = Integer.parseInt(nLine);
        } catch (NumberFormatException e) {
            requested = 3; // default
        }
        if (requested < 1) requested = 1;
        if (requested > 5) requested = 5;

        Preference pref = new Preference(genres, moods, includeNewArtists);

        // 4) Process the input (Phase 2: Process Song Recommendation – input side)
        ProcessedInput pi = handler.processInput(pref);
        System.out.println("\nProcessed input: " + pi);

        // 5) Use the recommender to build a RecommendationSet
        Recommender recommender = new Recommender();
        RecommendationSet set = recommender.recommend(pi, library);

        if (set.size() == 0) {
            System.out.println("\nNo recommendations matched your preferences.");
            System.out.println("Done. Process finished.");
            return;
        }

        // 6) Show the top N recommendations
        int n = Math.min(requested, set.size());
        List<Recommendation> top = set.getTopN(n);

        // Build a smaller set just for generating links
        RecommendationSet topSet = new RecommendationSet();
        for (Recommendation r : top) {
            topSet.addRecommendation(r);
        }
        var links = handler.getLinks(topSet); // Phase 2: Navigate to Spotify (via links)

        System.out.println("\nTop " + n + " recommendations:");
        List<Track> topTracks = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            Recommendation rec = top.get(i);
            Track track = findTrackById(library, rec.trackId());
            if (track != null) {
                topTracks.add(track);
            }

            String label = (track != null)
                    ? track.title() + " – " + track.artistName()
                    : "Track ID " + rec.trackId();

            System.out.printf("%d. %s%n", i + 1, label);
            System.out.printf("   Score: %.2f%n", rec.score());
            System.out.println("   Link:  " + links.get(i).url());
        }

        // 6b) AI explanation of the recommendations
        try {
            AiExplainer explainer = new AiExplainer();
            String explanation = explainer.explainRecommendations(pi, topTracks);

            System.out.println("\nAI explanation of these recommendations:");
            System.out.println(explanation.trim());
        } catch (Exception e) {
            System.out.println("\n[AI explanation unavailable: " + e.getMessage() + "]");
        }

        // 7) Optional: Download Recommendations (Phase 2: Download Recommendations)
        System.out.print("\nWould you like to export these recommendations as CSV? (yes/no): ");
        String exportAnswer = scanner.nextLine().trim().toLowerCase();
        if (exportAnswer.startsWith("y")) {
            RecommendationExporter exporter = new RecommendationExporter();
            try {
                var path = exporter.exportAsCsv(top, library, "recommendations.csv");
                System.out.println("Recommendations exported to: " + path.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to export recommendations: " + e.getMessage());
            }
        }

        System.out.println("\nDone. Process finished.");
    }

    // Helper to look up a Track by id
    private static Track findTrackById(List<Track> library, String id) {
        for (Track t : library) {
            if (t.id().equals(id)) {
                return t;
            }
        }
        return null;
    }
}
