package com.example.spotifyaisystem;

import java.util.List;

public class Artist {

    private final String id;
    private final String name;
    private final List<String> genres;
    private final int followers;
    private final int popularity;
    private final String link;

    public Artist(String id, String name, List<String> genres, int followers, int popularity) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.followers = followers;
        this.popularity = popularity;
        this.link = "https://open.spotify.com/artist/" + id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getGenres() {
        return genres;
    }

    public int getFollowers() {
        return followers;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getLink() {
        return link;
    }
}
