package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthHandler implements HttpHandler {
    private final UserService userService;
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^/api/auth/login$");
    private static final Pattern REGISTER_PATTERN = Pattern.compile("^/api/auth/register$");
    private static final Pattern LOGOUT_PATTERN = Pattern.compile("^/api/auth/logout$");
    private static final Pattern ME_PATTERN = Pattern.compile("^/api/auth/me$");

    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("POST") && LOGIN_PATTERN.matcher(path).matches()) {
            handleLogin(exchange);
        } else if (method.equals("POST") && REGISTER_PATTERN.matcher(path).matches()) {
            handleRegister(exchange);
        } else if (method.equals("POST") && LOGOUT_PATTERN.matcher(path).matches()) {
            handleLogout(exchange);
        } else if (method.equals("GET") && ME_PATTERN.matcher(path).matches()) {
            handleMe(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String username = extractField(body, "username");
        String password = extractField(body, "password");

        if (username == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Username and password required\"}");
            return;
        }

        User user = userService.login(username, password);
        if (user == null) {
            sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            return;
        }

        String token = UUID.randomUUID().toString();
        userService.createSession(token, user.getId());

        String response = "{\"success\":true,\"token\":\"" + token + "\"," + user.toJson().substring(1);
        sendResponse(exchange, 200, response);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String username = extractField(body, "username");
        String password = extractField(body, "password");
        String displayName = extractField(body, "displayName");

        if (username == null || password == null) {
            sendResponse(exchange, 400, "{\"error\":\"Username and password required\"}");
            return;
        }

        if (password.length() < 3) {
            sendResponse(exchange, 400, "{\"error\":\"Password too short\"}");
            return;
        }

        User user = userService.register(username, password, displayName);
        if (user == null) {
            sendResponse(exchange, 409, "{\"error\":\"Username already taken\"}");
            return;
        }

        String token = UUID.randomUUID().toString();
        userService.createSession(token, user.getId());

        String response = "{\"success\":true,\"token\":\"" + token + "\"," + user.toJson().substring(1);
        sendResponse(exchange, 201, response);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = getTokenFromHeader(exchange);
        if (token != null) {
            userService.removeSession(token);
        }
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = getTokenFromHeader(exchange);
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

        sendResponse(exchange, 200, user.toJson());
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        byte[] bytes = is.readAllBytes();
        is.close();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"([^\"]*)\"";
        Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String getTokenFromHeader(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        return token;
    }

    private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
