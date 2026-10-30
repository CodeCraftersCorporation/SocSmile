package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Post;
import service.PostService;
import service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeHandler implements HttpHandler {
    private final PostService postService;
    private final UserService userService;
    private static final Pattern LIKE_PATTERN = Pattern.compile("^/api/posts/(\\d+)/like$");

    public LikeHandler(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("POST") && LIKE_PATTERN.matcher(path).matches()) {
            handleLike(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleLike(HttpExchange exchange) throws IOException {
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

        Matcher m = LIKE_PATTERN.matcher(exchange.getRequestURI().getPath());
        if (!m.find()) {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }

        long postId = Long.parseLong(m.group(1));
        boolean currentlyLiked = postService.hasLiked(postId, userId);

        if (currentlyLiked) {
            postService.removeLike(postId, userId);
        } else {
            postService.addLike(postId, userId);
        }

        Post post = postService.getPostById(postId);
        if (post != null) {
            sendResponse(exchange, 200, post.toJson());
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Post not found\"}");
        }
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
