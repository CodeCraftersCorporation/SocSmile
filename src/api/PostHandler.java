package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Post;
import service.PostService;
import service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostHandler implements HttpHandler {
    private final PostService postService;
    private final UserService userService;
    private static final Pattern CREATE_PATTERN = Pattern.compile("^/api/posts$");
    private static final Pattern LIST_PATTERN = Pattern.compile("^/api/posts$");
    private static final Pattern USER_PATTERN = Pattern.compile("^/api/users/(\\d+)/posts$");

    public PostHandler(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && LIST_PATTERN.matcher(path).matches()) {
            handleGetPosts(exchange);
        } else if (method.equals("POST") && CREATE_PATTERN.matcher(path).matches()) {
            handleCreatePost(exchange);
        } else if (method.equals("GET") && USER_PATTERN.matcher(path).matches()) {
            handleGetUserPosts(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleGetPosts(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        Long userId = userService.getSessionUser(token);
        List<Post> posts = postService.getAllPosts();

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) sb.append(",");
            Post p = posts.get(i);
            if (userId != null) {
                p.setLiked(userId);
            }
            sb.append(p.toJson());
        }
        sb.append("]");
        sendResponse(exchange, 200, sb.toString());
    }

    private void handleCreatePost(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        Long userId = userService.getSessionUser(token);
        if (userId == null) {
            sendResponse(exchange, 401, "{\"error\":\"Invalid session\"}");
            return;
        }

        String body = readBody(exchange);
        String text = extractField(body, "text");
        String imageUrl = extractField(body, "imageUrl");

        if (text == null || text.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Post text required\"}");
            return;
        }

        Post post = postService.createPost(userId, text, imageUrl);
        post.setLiked(userId);
        String json = post.toJson();
        System.out.println("[PostHandler] Created post JSON length: " + json.length());
        System.out.println("[PostHandler] JSON preview: " + json.substring(0, Math.min(300, json.length())));
        sendResponse(exchange, 201, json);
    }

    private void handleGetUserPosts(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        Matcher m = USER_PATTERN.matcher(exchange.getRequestURI().getPath());
        if (!m.find()) {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }

        long userId = Long.parseLong(m.group(1));
        List<Post> posts = postService.getPostsByUser(userId);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(posts.get(i).toJson());
        }
        sb.append("]");
        sendResponse(exchange, 200, sb.toString());
    }

    private String readBody(HttpExchange exchange) throws IOException {
        var is = exchange.getRequestBody();
        byte[] bytes = is.readAllBytes();
        is.close();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"([^\"]*)\"";
        Matcher m = Pattern.compile(pattern).matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return exchange.getRequestHeaders().getFirst("X-Auth-Token");
    }

    private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
