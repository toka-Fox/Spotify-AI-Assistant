package com.example.spotifyaisystem;

public class OpenAIPullTest {
    public static void main(String[] args) {
        AiExplainer ai = new AiExplainer();
        String result = ai.simpleTest("Explain what a music recommendation system does in 2–3 sentences.");
        System.out.println("AI Response:");
        System.out.println(result);
    }
}
