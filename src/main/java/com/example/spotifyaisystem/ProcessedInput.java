package com.example.spotifyaisystem;

import java.util.List;

public record ProcessedInput(List<String> genres, List<String> moods, boolean includeNewArtists) {}
