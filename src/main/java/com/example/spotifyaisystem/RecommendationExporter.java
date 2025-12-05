package com.example.spotifyaisystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RecommendationExporter {

    /**
     * Export TRACK recommendations as CSV-like text.
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

    /**
     * Export ARTIST recommendations as CSV-like text.
     */
    public Path exportAsCsvArtists(List<Artist> artists, String fileName)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        int i = 1;
        for (Artist artist : artists) {
            sb.append(i++)
                    .append(" - ")
                    .append('"').append(artist.getName()).append('"')
                    .append("\n        Followers: ").append(artist.getFollowers())
                    .append("\n        Genres: ").append(String.join(", ", artist.getGenres()))
                    .append("\n        Popularity: ").append(artist.getPopularity())
                    .append("\n");
        }

        Path path = Path.of(fileName);
        Files.writeString(path, sb.toString());
        return path;
    }
}
