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
    public Path exportAsCsv(List<Track> tracks, String fileName)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        int i = 1;
        for (Track track : tracks) {
            sb.append(i++)
                    .append(" - ")
                    .append('"').append(track.getTitle()).append('"')
                    .append(" by ").append(track.getArtistName())
                    .append(": \n").append("        Score: ").append(track.getScore())
                    .append("\n").append("        Spotify link: ").append(track.getLink())
                    .append('\n');
        }

        Path path = Path.of(fileName);
        Files.writeString(path, sb.toString());
        return path;
    }
}
