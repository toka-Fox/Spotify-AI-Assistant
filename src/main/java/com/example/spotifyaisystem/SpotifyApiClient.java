package com.example.spotifyaisystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SpotifyApiClient {

    private final String clientId;
    private final String clientSecret;
    private final HttpClient http;

    public SpotifyApiClient() {
        this.clientId = ConfigLoader.get("SPOTIFY_CLIENT_ID");
        this.clientSecret = ConfigLoader.get("SPOTIFY_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Spotify client id/secret not set in config.properties.");
        }
        this.http = HttpClient.newHttpClient();
    }

    public Track searchTrack(String query) throws Exception {
        String token = requestAccessToken();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery +
                "&type=track&limit=1";  // ask Spotify for only one

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Spotify search failed: " + resp.statusCode() + " " + resp.body());
        }

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        JsonObject tracksObj = root.getAsJsonObject("tracks");
        JsonArray items = tracksObj.getAsJsonArray("items");

        if (items.isEmpty()) {
            return null;  // or throw exception
        }

        JsonObject item = items.get(0).getAsJsonObject();
        String id = item.get("id").getAsString();
        String name = item.get("name").getAsString();

        JsonArray artists = item.getAsJsonArray("artists");
        String artistName = !artists.isEmpty()
                ? artists.get(0).getAsJsonObject().get("name").getAsString()
                : "Unknown Artist";

        return new Track(id, name, artistName);
    }

    public Artist searchArtist(String query) throws Exception {
        String token = requestAccessToken();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery +
                "&type=artist&limit=1";  // ask Spotify for only one artist

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Spotify search failed: " + resp.statusCode() + " " + resp.body());
        }

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        JsonObject artistsObj = root.getAsJsonObject("artists");
        JsonArray items = artistsObj.getAsJsonArray("items");

        if (items.isEmpty()) {
            return null;  // or throw exception
        }

        JsonObject item = items.get(0).getAsJsonObject();
        String id = item.get("id").getAsString();
        String name = item.get("name").getAsString();

        // Spotify provides an array of genres
        JsonArray genresArray = item.getAsJsonArray("genres");
        List<String> genres = new ArrayList<>();
        if (genresArray != null) {
            for (int i = 0; i < genresArray.size(); i++) {
                genres.add(genresArray.get(i).getAsString());
            }
        }

        // Followers
        int followers = item.getAsJsonObject("followers").get("total").getAsInt();

        // Popularity
        int popularity = item.get("popularity").getAsInt();

        return new Artist(id, name, genres, followers, popularity);
    }


    private String requestAccessToken() throws Exception {
        String authString = clientId + ":" + clientSecret;
        String basicAuth = Base64.getEncoder()
                .encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Token request failed: " + resp.statusCode() + " " + resp.body());
        }

        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
        return json.get("access_token").getAsString();
    }
}
