package com.example.spotifyaisystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendationSetTest {

    @Test
    void testAddRecommendation() {
        RecommendationSet set = new RecommendationSet();
        set.addRecommendation(new Recommendation(1, 0.95, "song123"));

        assertEquals(1, set.size());
    }

    @Test
    void testGetTopN() {
        RecommendationSet set = new RecommendationSet();
        set.addRecommendation(new Recommendation(3, 0.70, "t3"));
        set.addRecommendation(new Recommendation(1, 0.95, "t1"));
        set.addRecommendation(new Recommendation(2, 0.80, "t2"));

        List<Recommendation> top2 = set.getTopN(2);

        assertEquals(2, top2.size());
        assertEquals(1, top2.get(0).rank());  // top recommendation
    }

    @Test
    void testSize() {
        RecommendationSet set = new RecommendationSet();
        assertEquals(0, set.size());

        set.addRecommendation(new Recommendation(1, 1.0, "x"));
        assertEquals(1, set.size());
    }
}
