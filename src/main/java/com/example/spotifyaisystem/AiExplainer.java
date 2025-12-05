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

        if (apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not set in config.properties.");
        }
    }

    public AiExplainer(OpenAIClient client) {
        this.client = client;
    }

    private String extractFirstText(ChatCompletion completion) {
        if (completion.choices().isEmpty() || completion.choices().isEmpty()) {
            return "";
        }

        ChatCompletion.Choice firstChoice = completion.choices().getFirst();

        if (!firstChoice.message().isValid()) {
            return "";
        }

        return firstChoice.message().content().orElse("");
    }

    public List<Track> getRecommendations(Preference pref, int count) {
        String query = "Give me " + count + " songs in the format of - '[SONG1]' [ARTIST1], '[SONG2]' [ARTIST2], etc. " +
                            "Do not output any other text other than that. The songs must have the following traits:\n" +
                            "In the genre of " + pref.genres() +",\n" +
                            "A mood of " + pref.moods() + ",\n" +
                            "In the music era of " + pref.eras();

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
                System.out.println(result);
                Track track = spotifyClient.searchTrack(result);
                fromSpotify.add(track);
            }

            if (!fromSpotify.isEmpty()) {
                System.out.println("Loaded " + fromSpotify.size() + " tracks from Spotify API.");
                return fromSpotify;
            } else {
                System.out.println("Spotify returned no tracks.");
            }
        } catch (Exception e) {
            System.out.println("API failed: " + e.getMessage());
        }

        return null;
    }

    public String explainRecommendations(Preference pref, List<Track> recommendedTracks) {
        String genres = String.join(", ", pref.genres());
        String moods = String.join(", ", pref.moods());
        String eras = String.join(", ", pref.eras());

        String trackList = recommendedTracks.stream()
                .map(t -> t.getTitle() + " by " + t.getArtistName() + " (id=" + t.getId() + ")")
                .collect(Collectors.joining("; "));

        String userPrompt =
                "Explain a simple music recommendation in a casual way.\n\n" +
                        "Genres: " + genres + "\n" +
                        "Moods: " + moods + "\n" +
                        "Eras: " + eras + "\n" +
                        "Recommended: " + trackList + "\n\n" +
                        "Explain in 3–5 sentences why these tracks match the genres and moods given.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addUserMessage(userPrompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return extractFirstText(completion);
    }

    public void scoreRecommendations(List<Track> tracks, Preference pref) {
        try {
            StringBuilder sb = new StringBuilder();

            for (Track track : tracks) {
                String s = '"' + track.getTitle() + '"' + " by " + track.getArtistName() + "\n";
                sb.append(s);
            }

            String userPrompt = "Take the following songs and give them a score 1 to 5 on how well they fit the genre and mood described. Dont be afraid to rate them low if needed." +
                    "Do not generate any other text. Only generate the numbers split by commas. Example: 5, 2, 3, 4, etc." +
                    "Genres: " + pref.genres() +
                    "Moods: " + pref.moods() +
                    "Eras: " + pref.eras() +
                    "Songs: " + sb;

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1_MINI)
                    .addUserMessage(userPrompt)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);

            String result = extractFirstText(completion);

            String[] scores = result.split(", ");
            for (int i = 0; i < scores.length; i++) {
                tracks.get(i).setScore(Integer.parseInt(scores[i]));
            }
        } catch (Exception e) {
            System.out.println("API call failed: " + e.getMessage());
        }
    }
}
