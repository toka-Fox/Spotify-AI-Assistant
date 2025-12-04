package com.example.spotifyaisystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RecommendationExporter {

    /**
     * Exports recommendations as a simple CSV file.
     * In Phase 2 terms, this is the "Download Recommendations" use case.
     */
    public Path exportAsCsv(List<Recommendation> recs, List<Track> library, String fileName)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        for (Recommendation r : recs) {
            Track track = findTrackById(library, r.trackId());
            String title = track != null ? track.title() : "";
            String artist = track != null ? track.artistName() : "";
            sb.append(r.rank())
                    .append(" - ")
                    .append('"').append(title).append('"')
                    .append(" by ").append(artist)
                    .append(": \n")
                    .append("        Score: " + r.score())
                    .append("\n")
                    .append("        Spotify link: https://open.spotify.com/track/" + r.trackId())
                    .append('\n');
        }

        Path path = Path.of(fileName);
        Files.writeString(path, sb.toString());
        return path;
    }

    private Track findTrackById(List<Track> library, String id) {
        for (Track t : library) {
            if (t.id().equals(id)) return t;
        }
        return null;
    }
}
