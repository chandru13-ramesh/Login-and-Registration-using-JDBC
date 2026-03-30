package util;

import java.util.regex.Pattern;

/**
 * ValidationUtil - Centralizes all input validation rules.
 *
 * Architecture: util layer — pure functions, no DB access, no UI calls.
 * The Service layer calls these before performing any DB operation.
 */
public class ValidationUtil {

    /**
     * RFC 5322-inspired email regex (practical subset).
     *
     * Matches:  user@example.com | john.doe+alias@sub.domain.org
     * Rejects:  @domain.com | user@ | plainaddress | user@.com
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Prevent instantiation
    private ValidationUtil() {}

    /**
     * Checks whether the given email matches the expected format.
     *
     * @param email the email address string to validate
     * @return true if the email format is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Ensures a username is non-null, non-blank, and within length limits.
     *
     * @param username the username to validate
     * @return true if valid (3–50 characters, alphanumeric + underscore only)
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) return false;
        // Only letters, digits, and underscores — length 3 to 50
        return username.trim().matches("^[A-Za-z0-9_]{3,50}$");
    }

    /**
     * Enforces minimum password strength.
     *
     * Rules:
     *  - At least 8 characters
     *  - At least one digit
     *  - At least one uppercase letter
     *  - At least one lowercase letter
     *  - At least one special character (@#$%^&+=!*)
     *
     * @param password the plain-text password to check
     * @return true if the password meets all strength requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "@#$%^&+=!*".indexOf(c) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
