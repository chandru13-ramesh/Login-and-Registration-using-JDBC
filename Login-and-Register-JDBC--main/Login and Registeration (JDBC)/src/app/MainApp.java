package app;

import model.User;
import service.AuthService;

import java.util.Scanner;

public class MainApp {

    // ANSI colour codes for richer console output (supported on most modern terminals)
    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BOLD   = "\u001B[1m";

    private static final Scanner scanner     = new Scanner(System.in);
    private static final AuthService authSvc = new AuthService();


    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleRegister();
                case "2" -> {
                    if (authSvc.isLocked()) {
                        printError("Account is locked due to too many failed attempts. Restart the app.");
                    } else {
                        handleLogin();
                    }
                }
                case "3" -> {
                    printInfo("Goodbye! Stay secure. 👋");
                    running = false;
                }
                default  -> printWarning("Invalid choice. Please enter 1, 2, or 3.");
            }
        }

        scanner.close();
    }


    private static void handleRegister() {
        printSectionHeader("USER REGISTRATION");

        System.out.print(CYAN + "  Enter username : " + RESET);
        String username = scanner.nextLine().trim();

        System.out.print(CYAN + "  Enter email    : " + RESET);
        String email = scanner.nextLine().trim();

        System.out.print(CYAN + "  Enter password : " + RESET);
        String password = scanner.nextLine().trim();

        System.out.print(CYAN + "  Confirm password: " + RESET);
        String confirmPassword = scanner.nextLine().trim();

        try {
            authSvc.register(username, email, password, confirmPassword);
            printSuccess("Registration successful! Welcome, " + username + "! 🎉");
            printInfo("You can now log in with your credentials.");

        } catch (IllegalArgumentException e) {
            // Validation or duplicate error — expected, show friendly message
            printError("Registration failed: " + e.getMessage());
        } catch (RuntimeException e) {
            // Unexpected error (e.g., DB down)
            printError("Unexpected error: " + e.getMessage());
        }
    }

    // ── Login Flow ────────────────────────────────────────────────────────────

    private static void handleLogin() {
        printSectionHeader("USER LOGIN");

        System.out.print(CYAN + "  Enter username : " + RESET);
        String username = scanner.nextLine().trim();

        System.out.print(CYAN + "  Enter password : " + RESET);
        String password = scanner.nextLine().trim();

        try {
            User user = authSvc.login(username, password);
            printSuccess("Login successful! Welcome back, " + user.getUsername() + "! 🔓");
            printInfo("Logged in as: " + user.getEmail());
            showUserDashboard(user);

        } catch (IllegalStateException e) {
            // Locked out
            printError("🔒 " + e.getMessage());

        } catch (IllegalArgumentException e) {
            // Wrong credentials
            printError("Login failed: " + e.getMessage());

        } catch (RuntimeException e) {
            printError("Unexpected error: " + e.getMessage());
        }
    }

    // ── Post-Login Dashboard ──────────────────────────────────────────────────

    /**
     * Shown after a successful login — in a real app this would route to features.
     */
    private static void showUserDashboard(User user) {
        System.out.println();
        System.out.println(BOLD + CYAN + "  ╔═══════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "  ║          USER DASHBOARD               ║" + RESET);
        System.out.println(BOLD + CYAN + "  ╠═══════════════════════════════════════╣" + RESET);
        System.out.printf (BOLD + CYAN + "  ║  ID       : %-26s║%n" + RESET, user.getId());
        System.out.printf (BOLD + CYAN + "  ║  Username : %-26s║%n" + RESET, user.getUsername());
        System.out.printf (BOLD + CYAN + "  ║  Email    : %-26s║%n" + RESET, user.getEmail());
        System.out.println(BOLD + CYAN + "  ╚═══════════════════════════════════════╝" + RESET);
        System.out.println();
        printInfo("Press ENTER to return to the main menu...");
        scanner.nextLine();
        // Reset attempts after a successful login session
        authSvc.resetAttempts();
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println(BOLD + GREEN +
            "  ╔══════════════════════════════════════════════╗\n" +
            "  ║     🔐  SECURE LOGIN & REGISTRATION SYSTEM   ║\n" +
            "  ║        Powered by Java + MySQL + BCrypt       ║\n" +
            "  ╚══════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println(BOLD + "  ──────────────────────────────────────────────" + RESET);
        System.out.println(BOLD + "                   MAIN MENU                   " + RESET);
        System.out.println(BOLD + "  ──────────────────────────────────────────────" + RESET);
        System.out.println("   [1]  Register");
        System.out.println("   [2]  Login");
        System.out.println("   [3]  Exit");
        System.out.println(BOLD + "  ──────────────────────────────────────────────" + RESET);
        System.out.print(CYAN + "  Your choice: " + RESET);
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "  ── " + title + " ─────────────────────────────" + RESET);
    }

    private static void printSuccess(String msg) {
        System.out.println(GREEN + "\n  ✔ " + msg + RESET);
    }

    private static void printError(String msg) {
        System.out.println(RED + "\n  ✘ " + msg + RESET);
    }

    private static void printWarning(String msg) {
        System.out.println(YELLOW + "\n  ⚠ " + msg + RESET);
    }

    private static void printInfo(String msg) {
        System.out.println(CYAN + "  ℹ " + msg + RESET);
    }
}
