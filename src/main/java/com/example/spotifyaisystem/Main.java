package com.example.spotifyaisystem;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1) Build a fake track library
        Track t1 = new Track("1", "Hello World", "Artist A");
        Track t2 = new Track("2", "Goodbye", "Artist B");
        Track t3 = new Track("3", "Happy Rock Song", "Artist C");
        List<Track> library = List.of(t1, t2, t3);

        // 2) Create the handler with this library
        InputHandler handler = new InputHandler(library);

        // 3) Pretend this is what the user likes
        Preference pref = new Preference(
                List.of("rock", "pop"),
                List.of("happy"),
                true
        );

        // 4) Process the input
        ProcessedInput pi = handler.processInput(pref);
        System.out.println("Processed input: " + pi);

        // 5) For now, just recommend everything with some fake ranks
        RecommendationSet set = new RecommendationSet();
        set.addRecommendation(new Recommendation(1, 0.95, t1.id()));
        set.addRecommendation(new Recommendation(2, 0.90, t2.id()));
        set.addRecommendation(new Recommendation(3, 0.85, t3.id()));

        // 6) Get Spotify links from the recommendations
        var links = handler.getLinks(set);

        System.out.println("Top links:");
        for (Link link : links) {
            System.out.println(" - " + link.url());
        }
    }
}
