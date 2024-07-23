package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class JsonPlaceholderClient1 {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static void main(String[] args) {
        try {
            getLastPostCommentsAndSaveToFile(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getLastPostCommentsAndSaveToFile(int userId) throws Exception {
        String postsUrl = BASE_URL + "/users/" + userId + "/posts";
        String postsResponse = sendRequest(postsUrl, "GET", null);

        List<Post> posts = parsePosts(postsResponse);
        if (posts.isEmpty()) {
            System.out.println("No posts found for user " + userId);
            return;
        }

        int lastPostId = posts.stream()
                .mapToInt(Post::getId)
                .max()
                .orElseThrow(() -> new RuntimeException("Failed to find the last post"));

        String commentsUrl = BASE_URL + "/posts/" + lastPostId + "/comments";
        String commentsResponse = sendRequest(commentsUrl, "GET", null);

        String filename = "user-" + userId + "-post-" + lastPostId + "-comments.json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(commentsResponse);
        }

        System.out.println("Comments of the last post of user " + userId + " have been saved to " + filename);
    }

    private static List<Post> parsePosts(String postsJson) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(postsJson, new TypeToken<List<Post>>() {}.getType());
        } catch (Exception e) {
            System.err.println("Failed to parse posts JSON: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Повертає порожній список у випадку помилки
        }
    }

    private static String sendRequest(String urlString, String method, String jsonInput) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(jsonInput != null);

        if (jsonInput != null) {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                throw new RuntimeException("Failed : HTTP error code : " + responseCode + " Response: " + errorResponse.toString());
            }
        }
    }

    private static class Post {
        private int id;
        private int userId;
        private String title;
        private String body;

        public int getId() {
            return id;
        }

        // Додайте інші геттери та сеттери, якщо потрібно
    }
}
