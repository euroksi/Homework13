package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class JsonPlaceholderClient2 {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            createUser(new User(0, "New User", "username", "user@example.com", "New Address", "123-456-7890"));
            updateUser(new User(1, "Updated User", "username", "user@example.com", "Updated Address", "123-456-7890"));
            deleteUser(1);
            List<User> users = getAllUsers();
            System.out.println(users);
            User userById = getUserById(1);
            System.out.println(userById);
            User userByUsername = getUserByUsername("username");
            System.out.println(userByUsername);
            printOpenTasksForUser(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createUser(User user) throws Exception {
        String url = BASE_URL + "/users";
        String userJson = gson.toJson(user);
        String response = sendRequest(url, "POST", userJson);
        User createdUser = gson.fromJson(response, User.class);
        System.out.println("Created user: " + createdUser);
    }

    public static void updateUser(User user) throws Exception {
        String url = BASE_URL + "/users/" + user.getId();
        String userJson = gson.toJson(user);
        String response = sendRequest(url, "PUT", userJson);
        User updatedUser = gson.fromJson(response, User.class);
        System.out.println("Updated user: " + updatedUser);
    }

    public static void deleteUser(int userId) throws Exception {
        String url = BASE_URL + "/users/" + userId;
        sendRequest(url, "DELETE", null);
        System.out.println("Deleted user with ID: " + userId);
    }

    public static List<User> getAllUsers() throws Exception {
        String url = BASE_URL + "/users";
        String response = sendRequest(url, "GET", null);
        return gson.fromJson(response, new TypeToken<List<User>>() {}.getType());
    }

    public static User getUserById(int userId) throws Exception {
        String url = BASE_URL + "/users/" + userId;
        String response = sendRequest(url, "GET", null);
        return gson.fromJson(response, User.class);
    }

    public static User getUserByUsername(String username) throws Exception {
        String url = BASE_URL + "/users?username=" + username;
        String response = sendRequest(url, "GET", null);
        List<User> users = gson.fromJson(response, new TypeToken<List<User>>() {}.getType());
        return users.isEmpty() ? null : users.get(0);
    }

    public static void printOpenTasksForUser(int userId) throws Exception {
        String url = BASE_URL + "/users/" + userId + "/todos";
        String response = sendRequest(url, "GET", null);
        List<Task> tasks = gson.fromJson(response, new TypeToken<List<Task>>() {}.getType());
        List<Task> openTasks = tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
        openTasks.forEach(System.out::println);
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
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }

    private static class User {
        private int id;
        private String name;
        private String username;
        private String email;
        private String address;
        private String phone;

        public User(int id, String name, String username, String email, String address, String phone) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.email = email;
            this.address = address;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", address='" + address + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
        }
    }

    private static class Task {
        private int userId;
        private int id;
        private String title;
        private boolean completed;

        public int getId() {
            return id;
        }

        public boolean isCompleted() {
            return completed;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "userId=" + userId +
                    ", id=" + id +
                    ", title='" + title + '\'' +
                    ", completed=" + completed +
                    '}';
        }
    }
}
