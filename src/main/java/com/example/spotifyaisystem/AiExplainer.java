package com.example.spotifyaisystem;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

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

    /**
     * Simple test method.
     */
    public String simpleTest(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)   // newest cheap model
                .addUserMessage(prompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return extractFirstText(completion);
    }

    /**
     * Main explanation method.
     */
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
                        "Explain in 3–5 sentences why these tracks match.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addUserMessage(userPrompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return extractFirstText(completion);
    }
}
