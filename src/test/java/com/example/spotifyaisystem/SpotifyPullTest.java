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
        System.out.println(track);
    }
}
