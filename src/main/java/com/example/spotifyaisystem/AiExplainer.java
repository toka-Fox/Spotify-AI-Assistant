package com.example.spotifyaisystem;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class AiExplainer {

    private final OpenAIClient client;

    public AiExplainer() {
        this.client = OpenAIOkHttpClient.fromEnv();
    }

    public AiExplainer(OpenAIClient client) {
        this.client = client;
    }

    /**
     * Extracts text from a ChatCompletion.
     * Works with new SDK (message.content() returns Optional<String>).
     */
    private String extractFirstText(ChatCompletion completion) {
        if (completion.choices() == null || completion.choices().isEmpty()) {
            return "";
        }

        var firstChoice = completion.choices().get(0);

        if (firstChoice.message() == null) {
            return "";
        }

        // content() is Optional<String> in your SDK version
        return firstChoice.message().content().orElse("");
    }

    public LibrarySnapshot getRecommendations(String genre, String mood) {

        String query = "Give me a list of five songs in the format of - '[SONG1]' [ARTIST1], '[SONG2]' [ARTIST2], etc. " +
                            "Do not output any other text other than that. The songs must have the following traits:\n" +
                            "In the genre of " + genre +",\n" +
                            "A mood of " + mood;

        // 1) Try real Spotify API
        try {
            SpotifyApiClient spotifyClient = new SpotifyApiClient();

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1_MINI)
                    .addUserMessage(query)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            String output = extractFirstText(completion);

            String[] results = output.split(", ");

            List<Track> fromSpotify = new java.util.ArrayList<>(List.of());

            for (String result : results) {
                Track track = spotifyClient.searchTrack(result);
                fromSpotify.add(track);
            }

            if (!fromSpotify.isEmpty()) {
                System.out.println("Loaded " + fromSpotify.size() + " tracks from Spotify API.");
                return new LibrarySnapshot(fromSpotify, Instant.now());
            } else {
                System.out.println("Spotify returned no tracks.");
            }
        } catch (Exception e) {
            System.out.println("API failed: " + e.getMessage());
        }

        return null;
    }

    public String explainRecommendations(ProcessedInput input, List<Track> recommendedTracks) {
        String genres = String.join(", ", input.genres());
        String moods = String.join(", ", input.moods());

        String trackList = recommendedTracks.stream()
                .map(t -> t.title() + " by " + t.artistName() + " (id=" + t.id() + ")")
                .collect(Collectors.joining("; "));

        String userPrompt =
                "Explain a simple music recommendation in a casual way.\n\n" +
                        "Genres: " + genres + "\n" +
                        "Moods: " + moods + "\n" +
                        "Include new artists? " + input.includeNewArtists() + "\n" +
                        "Recommended: " + trackList + "\n\n" +
                        "Explain in 3–5 sentences why these tracks match the genres and moods given.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addUserMessage(userPrompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return extractFirstText(completion);
    }
}
