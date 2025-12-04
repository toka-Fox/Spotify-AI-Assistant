package com.example.spotifyaisystem;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import com.sun.net.httpserver.HttpServer;

public class SpotifyOAuth {

    private static final String CLIENT_ID = System.getenv("SPOTIFY_CLIENT_ID");
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    private static String codeVerifier;

    public static void startLogin() throws Exception {
        // 1. Generate PKCE verifier
        codeVerifier = generateCodeVerifier();
        String codeChallenge = codeVerifier; // using plain method for simplicity

        // 2. Build authorization URL
        String url = "https://accounts.spotify.com/authorize?" +
                "client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&code_challenge_method=plain" +
                "&code_challenge=" + codeChallenge +
                "&scope=user-read-email"; // add scopes you need

        // 3. Start local callback listener
        startCallbackServer();

        // 4. Open browser
        Desktop.getDesktop().browse(URI.create(url));
    }

    private static void startCallbackServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = query.split("code=")[1];

            // Respond to browser
            String resp = "Login successful! You can close this window.";
            exchange.sendResponseHeaders(200, resp.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp.getBytes());
            }

            server.stop(0);

            // Exchange code for access token
            try {
                exchangeCodeForToken(code);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        server.start();
    }

    private static void exchangeCodeForToken(String code) throws Exception {
        HttpClient http = HttpClient.newHttpClient();

        String body =
                "grant_type=authorization_code" +
                        "&code=" + code +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                        "&client_id=" + CLIENT_ID +
                        "&code_verifier=" + codeVerifier;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

        String accessToken = json.get("access_token").getAsString();

        System.out.println("User Access Token: " + accessToken);

        // TODO: store token for your other API uses
    }

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
