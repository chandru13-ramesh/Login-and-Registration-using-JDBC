package util;

import org.mindrot.jbcrypt.BCrypt;


public class PasswordUtil {
    private static final int BCRYPT_ROUNDS = 12;

    // Prevent instantiation
    private PasswordUtil() {}

    /**
     * Hashes a plain-text password with BCrypt + random salt.
     *
     * @param plainTextPassword the raw password entered by the user
     * @return a 60-character BCrypt hash string, safe to store in the DB
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainTextPassword the raw password entered at login
     * @param hashedPassword    the BCrypt hash fetched from the DB
     * @return true if they match, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        // BCrypt.checkpw internally extracts the salt from the stored hash
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
