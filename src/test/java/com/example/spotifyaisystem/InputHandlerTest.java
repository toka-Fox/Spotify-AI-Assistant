package com.example.spotifyaisystem;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InputHandlerTest {

    @Test
    void testProcessInput() {
        Preference pref = new Preference(
                List.of("rock", "pop"),
                List.of("happy"),
                true
        );

        InputHandler handler = new InputHandler(List.of());
        ProcessedInput pi = handler.processInput(pref);

        assertEquals(List.of("rock", "pop"), pi.genres());
        assertEquals(List.of("happy"), pi.moods());
        assertTrue(pi.includeNewArtists());
    }

    @Test
    void testFindSongFound() {
        Track t1 = new Track("1", "Hello World", "Artist A");
        Track t2 = new Track("2", "Goodbye", "Artist B");

        InputHandler handler = new InputHandler(List.of(t1, t2));

        Track result = handler.findSong("hello");
        assertNotNull(result);
        assertEquals("1", result.id());
    }

    @Test
    void testFindSongNotFound() {
        Track t1 = new Track("1", "Hello World", "Artist A");
        InputHandler handler = new InputHandler(List.of(t1));

        Track result = handler.findSong("xyz");
        assertNull(result);
    }
}
