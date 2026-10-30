package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Post;
import model.User;
import service.PostService;
import service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileHandler implements HttpHandler {
    private final UserService userService;
    private final PostService postService;
    private static final Pattern PROFILE_PATTERN = Pattern.compile("^/api/profile/(\\d+)$");
    private static final Pattern PROFILE_ME_PATTERN = Pattern.compile("^/api/profile/me$");

    public ProfileHandler(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (PROFILE_ME_PATTERN.matcher(path).matches()) {
            handleMe(exchange);
        } else if (PROFILE_PATTERN.matcher(path).matches()) {
            handleProfile(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleMe(HttpExchange exchange) throws IOException {
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

        User user = userService.getUserById(userId);
        if (user == null) {
            sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            return;
        }

        List<Post> posts = postService.getPostsByUser(userId);
        StringBuilder postsJson = new StringBuilder("[");
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) postsJson.append(",");
            postsJson.append(posts.get(i).toJson());
        }
        postsJson.append("]");

        String response = "{\"user\":" + user.toJson() + ",\"posts\":" + postsJson + "}";
        sendResponse(exchange, 200, response);
    }

    private void handleProfile(HttpExchange exchange) throws IOException {
        String token = getToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        Matcher m = PROFILE_PATTERN.matcher(exchange.getRequestURI().getPath());
        if (!m.find()) {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }

        long userId = Long.parseLong(m.group(1));
        User user = userService.getUserById(userId);
        if (user == null) {
            sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            return;
        }

        List<Post> posts = postService.getPostsByUser(userId);
        StringBuilder postsJson = new StringBuilder("[");
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) postsJson.append(",");
            postsJson.append(posts.get(i).toJson());
        }
        postsJson.append("]");

        String response = "{\"user\":" + user.toJson() + ",\"posts\":" + postsJson + "}";
        sendResponse(exchange, 200, response);
    }

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return exchange.getRequestHeaders().getFirst("X-Auth-Token");
    }

    private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
