package controller;

import api.AuthHandler;
import api.LikeHandler;
import api.PostHandler;
import api.ProfileHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpServerManager {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private final HttpServer server;
    private final service.UserService userService;
    private final service.PostService postService;

    // Статика — из classpath (работает и локально, и из JAR)
    private static final String STATIC_CLASSPATH = "/static/";
    private static Path STATIC_DIR;

    static {
        try {
            // Пробуем загрузить из classpath
            URL staticUrl = HttpServerManager.class.getResource(STATIC_CLASSPATH);
            if (staticUrl != null) {
                if ("file".equals(staticUrl.getProtocol())) {
                    STATIC_DIR = staticUrl.toURI().toPath();
                    System.out.println("[Static] Loaded from: " + STATIC_DIR);
                } else {
                    // JAR — копируем в temp
                    STATIC_DIR = extractFromClasspath(staticUrl);
                    System.out.println("[Static] Extracted to temp: " + STATIC_DIR);
                }
            } else {
                // Фоллбэк — файловая система
                STATIC_DIR = Path.of("src/main/resources/static");
                System.out.println("[Static] Using filesystem: " + STATIC_DIR);
            }
        } catch (Exception e) {
            STATIC_DIR = Path.of("src/main/resources/static");
            System.out.println("[Static] Fallback to: " + STATIC_DIR);
        }
    }

    private static Path extractFromClasspath(URL url) throws IOException {
        Path temp = Files.createTempDirectory("socsmile-static");
        copyFromJar(url, temp);
        return temp;
    }

    private static void copyFromJar(URL url, Path target) throws IOException {
        if (url.getProtocol().equals("jar")) {
            try (var is = url.openStream()) {
                Files.copy(is, target.resolve("index.html"));
            }
        } else {
            Files.copy(url.openStream(), target.resolve("index.html"));
        }
        // Копируем все файлы из директории
        java.nio.file.Files.list(target)
            .filter(p -> p.toFile().isFile())
            .forEach(p -> {});
    }

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put(".html", "text/html; charset=utf-8");
        MIME_TYPES.put(".css", "text/css; charset=utf-8");
        MIME_TYPES.put(".js", "application/javascript; charset=utf-8");
        MIME_TYPES.put(".json", "application/json; charset=utf-8");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".svg", "image/svg+xml");
        MIME_TYPES.put(".ico", "image/x-icon");
    }

    public HttpServerManager() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.userService = new service.UserService();
        this.postService = new service.PostService(userService);
        setupRoutes();
    }

    private void setupRoutes() {
        server.createContext("/api/auth", exchange -> new AuthHandler(userService).handle(exchange));
        server.createContext("/api/posts", exchange -> new PostHandler(postService, userService).handle(exchange));
        server.createContext("/api/posts/like", exchange -> new LikeHandler(postService, userService).handle(exchange));
        server.createContext("/api/profile", exchange -> new ProfileHandler(userService, postService).handle(exchange));
        server.createContext("/", this::serveStatic);
        server.setExecutor(null);
    }

    private void serveStatic(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/") || path.equals("/index.html")) {
            path = "index.html";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Path filePath = STATIC_DIR.resolve(path).normalize();
        if (!filePath.startsWith(STATIC_DIR)) {
            send404(exchange, path);
            return;
        }

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            String mimeType = getMimeType(path);
            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            // SPA fallback
            Path indexPath = STATIC_DIR.resolve("index.html");
            if (Files.exists(indexPath)) {
                byte[] bytes = Files.readAllBytes(indexPath);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                send404(exchange, path);
            }
        }
    }

    private String getMimeType(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) {
            return MIME_TYPES.getOrDefault(filename.substring(dot), "application/octet-stream");
        }
        return "text/html; charset=utf-8";
    }

    private void send404(com.sun.net.httpserver.HttpExchange exchange, String path) throws IOException {
        String body = "<h1>404 Not Found</h1><p>" + escapeHtml(path) + "</p>";
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void start() {
        server.start();
        System.out.println("SocSmile running on http://localhost:" + PORT);
    }

    public void stop() {
        server.stop(1);
    }

    public static void main(String[] args) throws IOException {
        HttpServerManager manager = new HttpServerManager();
        manager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            manager.stop();
        }));
    }
}
