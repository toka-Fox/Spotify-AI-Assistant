package com.example.spotifyaisystem;

import java.time.Instant;
import java.util.List;

public record LibrarySnapshot(List<Track> tracks, Instant importedAt) {}
