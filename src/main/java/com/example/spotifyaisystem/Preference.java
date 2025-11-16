package com.example.spotifyaisystem;

import java.util.List;

public record Preference(List<String> genres, List<String> moods, boolean includeNewArtists) {}
