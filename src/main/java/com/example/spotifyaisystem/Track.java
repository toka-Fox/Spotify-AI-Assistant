package com.example.spotifyaisystem;

public class Track {
    private final String id;
    private final String link;
    private final String title;
    private final String artistName;
    private int score;

    public Track(String id, String title, String artistName) {
        this.id = id;
        this.link = "https://open.spotify.com/track/" + id;
        this.title = title;
        this.artistName = artistName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getScore() {
        return score;
    }
}
