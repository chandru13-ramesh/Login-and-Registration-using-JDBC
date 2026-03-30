package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthService - Orchestrates all authentication business logic.
 *
 * Architecture: Service layer - sits between the UI (MainApp) and the DB (UserDAO).
 * Responsibilities:
 *   - Input validation (delegates to ValidationUtil)
 *   - Password hashing/verification (delegates to PasswordUtil)
 *   - Duplicate-user detection
 *   - Login attempt tracking (max 3 per session)
 *   - Throwing meaningful exceptions to the UI layer
 *
 * Never touches raw SQL - that stays in UserDAO.
 */
public class AuthService {

    /** Maximum failed login attempts before the account is temporarily locked */
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    /** Tracks the number of consecutive failed logins for the current session */
    private int loginAttempts = 0;

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    // -- Registration ---------------------------------------------------------

    /**
     * Registers a new user after performing all validation and duplicate checks.
     *
     * Steps:
     *  1. Validate username format
     *  2. Validate email format
     *  3. Validate password strength
     *  4. Confirm passwords match
     *  5. Check for duplicate username / email
     *  6. Hash password with BCrypt
     *  7. Persist to DB via DAO
     *
     * @param username        desired username
     * @param email           user's email address
     * @param password        chosen password (plain text)
     * @param confirmPassword re-entered password for confirmation
     * @throws IllegalArgumentException if any validation fails
     * @throws RuntimeException         if a DB error occurs
     */
    public void register(String username, String email, String password, String confirmPassword) {

        // -- 1. Validate username ---------------------------------------------
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException(
                "Invalid username. Use 3-50 characters (letters, digits, underscore only).");
        }

        // -- 2. Validate email ------------------------------------------------
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException(
                "Invalid email format. Example: user@example.com");
        }

        // -- 3. Validate password strength ------------------------------------
        if (!ValidationUtil.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                "Weak password. Must be 8+ chars with uppercase, lowercase, digit, and special char (@#$%^&+=!*).");
        }

        // -- 4. Confirm passwords match ---------------------------------------
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                "Passwords do not match. Please try again.");
        }

        try {
            // -- 5. Duplicate checks ------------------------------------------
            if (userDAO.usernameExists(username)) {
                throw new IllegalArgumentException(
                    "Username '" + username + "' is already taken. Choose a different one.");
            }
            if (userDAO.emailExists(email)) {
                throw new IllegalArgumentException(
                    "Email '" + email + "' is already registered. Try logging in instead.");
            }

            // -- 6. Hash password ---------------------------------------------
            String hashedPassword = PasswordUtil.hashPassword(password);

            // -- 7. Persist ---------------------------------------------------
            User newUser = new User(username, email, hashedPassword);
            boolean saved = userDAO.registerUser(newUser);

            if (!saved) {
                throw new RuntimeException("Registration failed due to an unexpected database error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error during registration: " + e.getMessage(), e);
        }
    }

    // -- Login ----------------------------------------------------------------

    /**
     * Authenticates a user by username and password.
     *
     * Steps:
     *  1. Check login attempt limit (max 3)
     *  2. Look up user by username
     *  3. Verify BCrypt password
     *  4. Reset attempt counter on success
     *
     * @param username  the username entered by the user
     * @param password  the plain-text password entered by the user
     * @return the authenticated {@link User} object
     * @throws IllegalStateException    if the max login attempts have been exceeded
     * @throws IllegalArgumentException if credentials are invalid
     * @throws RuntimeException         if a DB error occurs
     */
    public User login(String username, String password) {

        // -- 1. Attempt limit guard -------------------------------------------
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            throw new IllegalStateException(
                "Account locked. Too many failed login attempts (" + MAX_LOGIN_ATTEMPTS + "). " +
                "Please restart the application.");
        }

        try {
            // -- 2. Fetch user by username -------------------------------------
            Optional<User> optionalUser = userDAO.findByUsername(username);

            if (optionalUser.isEmpty()) {
                loginAttempts++;
                throw new IllegalArgumentException(
                    "Invalid credentials. (" + remainingAttempts() + " attempt(s) remaining)");
            }

            User user = optionalUser.get();

            // -- 3. Verify BCrypt password -------------------------------------
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                loginAttempts++;
                throw new IllegalArgumentException(
                    "Invalid credentials. (" + remainingAttempts() + " attempt(s) remaining)");
            }

            // -- 4. Success - reset counter -----------------------------------
            loginAttempts = 0;
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during login: " + e.getMessage(), e);
        }
    }

    // -- Helpers --------------------------------------------------------------

    /** Returns how many login attempts are left before lockout */
    private int remainingAttempts() {
        return MAX_LOGIN_ATTEMPTS - loginAttempts;
    }

    /** Exposes attempt count so the UI layer can react to lockout state */
    public boolean isLocked() {
        return loginAttempts >= MAX_LOGIN_ATTEMPTS;
    }

    /** Resets login attempt counter (e.g., when user returns to main menu) */
    public void resetAttempts() {
        loginAttempts = 0;
    }
}
