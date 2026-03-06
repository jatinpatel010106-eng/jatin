package com.syllabus.app.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpPingClient {
    private HttpPingClient() {
    }

    public static String fetch(String rawUrl) throws Exception {
        URL url = new URL(rawUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.lineSeparator());
            }
        }
        return "HTTP " + conn.getResponseCode() + "\n" + response;
    }
}
