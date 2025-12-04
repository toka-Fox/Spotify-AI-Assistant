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
        String apiKey = ConfigLoader.get("OPENAI_API_KEY");
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

        if (apiKey == null) {
            throw new IllegalStateException("OpenAI API key is not set in config.properties.");
        }
    }

    public AiExplainer(OpenAIClient client) {
        this.client = client;
    }

    private String extractFirstText(ChatCompletion completion) {
        if (completion.choices() == null || completion.choices().isEmpty()) {
            return "";
        }

        var firstChoice = completion.choices().get(0);

        if (firstChoice.message() == null) {
            return "";
        }

        return firstChoice.message().content().orElse("");
    }

    public LibrarySnapshot getRecommendations(String genre, String mood) {

        String query = "Give me a list of five songs in the format of - '[SONG1]' [ARTIST1], '[SONG2]' [ARTIST2], etc. " +
                            "Do not output any other text other than that. The songs must have the following traits:\n" +
                            "In the genre of " + genre +",\n" +
                            "A mood of " + mood;

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
                        "Recommended: " + trackList + "\n\n" +
                        "Explain in 3–5 sentences why these tracks match the genres and moods given.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addUserMessage(userPrompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return extractFirstText(completion);
    }

    public String scoreRecommendations(Track track, ProcessedInput input) {
        try {
            String userPrompt = "Take the following song and give it a score 1 to 5 on how well it fits the genre and mood described. Dont be afraid to rate it low if needed." +
                    "Do not generate any other text. Only generate the number." +
                    "Genres: " + input.genres() +
                    "Moods: " + input.moods() +
                    "Song: " + track.title() + " by " + track.artistName();

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1_MINI)
                    .addUserMessage(userPrompt)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);

            return extractFirstText(completion);
        } catch (Exception e) {
            System.out.println("API call failed: " + e.getMessage());
        }

        return null;
    }
}
