package com.example.spotifyaisystem;

import java.util.List;

public class SpotifyPullTest {
    public static void main(String[] args) {
        LibraryImporter importer = new LibraryImporter();
        LibrarySnapshot snapshot = importer.importFromSpotify();

        System.out.println("Imported " + snapshot.tracks().size()
                + " tracks at " + snapshot.importedAt());

        List<Track> tracks = snapshot.tracks();
        for (int i = 0; i < Math.min(5, tracks.size()); i++) {
            Track t = tracks.get(i);
            System.out.println((i + 1) + ". " + t.title() + " – " + t.artistName()
                    + " (id=" + t.id() + ")");
        }
    }
}
