package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.User;
import service.AuthService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * WebServer - Lightweight HTTP server using built-in Java APIs (no extra JARs needed).
 *
 * Endpoints:
 *   GET  /              → serves web/index.html
 *   POST /api/register  → delegates to AuthService.register()
 *   POST /api/login     → delegates to AuthService.login()
 *
 * Run this class as the Java Application entry point to use the browser UI.
 * Open http://localhost:8080 after starting.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static final AuthService authService = new AuthService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",             new StaticHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login",    new LoginHandler());
        server.setExecutor(null); // default executor
        server.start();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Login & Registration  –  Server Started  ║");
        System.out.println("║  Open: http://localhost:" + PORT + "            ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("[INFO] Working directory: " + System.getProperty("user.dir"));
    }

    // ── Static File Handler ──────────────────────────────────────────────────

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String reqPath = ex.getRequestURI().getPath();
            if (reqPath.equals("/") || reqPath.equals("/index.html")) {
                byte[] bytes = readIndexHtml();
                ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, bytes.length);
                ex.getResponseBody().write(bytes);
            } else {
                byte[] bytes = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, bytes.length);
                ex.getResponseBody().write(bytes);
            }
            ex.getResponseBody().close();
        }
    }

    /**
     * Tries multiple candidate paths for web/index.html so the server works
     * regardless of what Eclipse sets as the working directory.
     */
    private static byte[] readIndexHtml() throws IOException {
        // Candidate locations to try, in priority order
        String[] candidates = {
            "web/index.html",                          // working dir IS project root
            "../web/index.html",                       // working dir is inside project
            "Login and Registeration (JDBC)/web/index.html",           // working dir is Desktop
            System.getProperty("user.home") +
                "/OneDrive/Desktop/Login and Registeration (JDBC)/web/index.html",  // absolute fallback
            "C:/Users/rajak/OneDrive/Desktop/Login and Registeration (JDBC)/web/index.html" // hard fallback
        };
        for (String candidate : candidates) {
            Path p = Paths.get(candidate);
            if (Files.exists(p)) {
                System.out.println("[INFO] Serving: " + p.toAbsolutePath());
                return Files.readAllBytes(p);
            }
        }
        throw new IOException(
            "Cannot find web/index.html. Working dir: " + System.getProperty("user.dir") +
            "\nFix: Right-click WebServer > Run As > Run Configurations > Arguments tab" +
            " > Working Directory > Other > Browse Workspace > select project root."
        );
    }

    // ── Register Handler ─────────────────────────────────────────────────────

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (isPreflight(ex)) return;
            if (!isPost(ex)) { sendJson(ex, 405, err("Method Not Allowed")); return; }

            String body            = readBody(ex);
            String username        = field(body, "username");
            String email           = field(body, "email");
            String password        = field(body, "password");
            String confirmPassword = field(body, "confirmPassword");

            try {
                authService.register(username, email, password, confirmPassword);
                sendJson(ex, 200,
                    "{\"success\":true,\"message\":\"Registration successful! Welcome, "
                    + escape(username) + "! 🎉\"}");
            } catch (IllegalArgumentException e) {
                sendJson(ex, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (RuntimeException e) {
                sendJson(ex, 500, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    // ── Login Handler ────────────────────────────────────────────────────────

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (isPreflight(ex)) return;
            if (!isPost(ex)) { sendJson(ex, 405, err("Method Not Allowed")); return; }

            String body     = readBody(ex);
            String username = field(body, "username");
            String password = field(body, "password");

            try {
                User user = authService.login(username, password);
                sendJson(ex, 200,
                    "{\"success\":true," +
                    "\"message\":\"Welcome back, " + escape(user.getUsername()) + "! 🔓\"," +
                    "\"user\":{" +
                        "\"id\":"        + user.getId()          + "," +
                        "\"username\":\"" + escape(user.getUsername()) + "\"," +
                        "\"email\":\""   + escape(user.getEmail())    + "\"" +
                    "}}");
            } catch (IllegalStateException e) {
                sendJson(ex, 423, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (IllegalArgumentException e) {
                sendJson(ex, 401, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (RuntimeException e) {
                sendJson(ex, 500, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    // ── Shared Utilities ─────────────────────────────────────────────────────

    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static boolean isPreflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static boolean isPost(HttpExchange ex) {
        return "POST".equalsIgnoreCase(ex.getRequestMethod());
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Extracts a string field value from a simple JSON object string.
     * Handles escaped characters inside values.
     */
    private static String field(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return "";
        idx++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (idx < json.length() && json.charAt(idx) != '"') {
            char c = json.charAt(idx);
            if (c == '\\' && idx + 1 < json.length()) {
                idx++;
                sb.append(json.charAt(idx));
            } else {
                sb.append(c);
            }
            idx++;
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String err(String msg) {
        return "{\"success\":false,\"message\":\"" + escape(msg) + "\"}";
    }
}
