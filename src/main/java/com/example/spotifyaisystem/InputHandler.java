package com.example.spotifyaisystem;

import java.util.ArrayList;
import java.util.List;

public class InputHandler {

    private final List<Track> library;

    public InputHandler(List<Track> library) {
        this.library = library;
    }

    // 1) processInput()
    public ProcessedInput processInput(Preference pref) {
        List<String> genres = pref.genres();
        List<String> moods = pref.moods();
        boolean include = pref.includeNewArtists();

        return new ProcessedInput(genres, moods, include);
    }

    // 2) getLinks()
    public List<Link> getLinks(RecommendationSet set) {
        List<Link> links = new ArrayList<>();
        for (Recommendation rec : set.getAll()) {
            links.add(new Link("https://open.spotify.com/track/" + rec.trackId()));
        }
        return links;
    }

    // 3) findSong()
    public Track findSong(String query) {
        if (query == null || query.isBlank()) return null;

        for (Track t : library) {
            if (t.title().toLowerCase().contains(query.toLowerCase())) {
                return t;
            }
        }
        return null;
    }
}
