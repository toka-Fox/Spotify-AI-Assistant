package com.example.spotifyaisystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpotifyPullTest {
    SpotifyApiClient api;
    @BeforeEach
    public void setUp() {
        api = new SpotifyApiClient();
    }

    @Test
    public void searchTrackTest() throws Exception {
        Track track = api.searchTrack("'commatose' glass beach");
        System.out.println(track.getTitle() + " " + track.getArtistName() + " " + track.getId());
    }

    @Test
    public void searchArtistTest() throws Exception {
        Artist artist = api.searchArtist("minor issue");
        System.out.println(artist.getName() + " with " + artist.getFollowers() + " followers and " + artist.getPopularity() + " popularity");
    }
}
