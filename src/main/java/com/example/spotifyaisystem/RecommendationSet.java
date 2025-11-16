package com.example.spotifyaisystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecommendationSet {

    private final List<Recommendation> list = new ArrayList<>();

    // 1) addRecommendation()
    public void addRecommendation(Recommendation rec) {
        if (rec != null) list.add(rec);
    }

    // 2) getTopN()
    public List<Recommendation> getTopN(int n) {
        return list.stream()
                .sorted(Comparator.comparingInt(Recommendation::rank))
                .limit(n)
                .toList();
    }

    // 3) size()
    public int size() {
        return list.size();
    }

    // helper for InputHandler
    public List<Recommendation> getAll() {
        return List.copyOf(list);
    }
}
