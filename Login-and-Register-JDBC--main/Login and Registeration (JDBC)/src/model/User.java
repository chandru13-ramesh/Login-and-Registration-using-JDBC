package model;

/**
 * User - Plain domain model (POJO) representing a registered user.
 *
 * Architecture: model layer — pure data carrier, zero logic.
 * The password field always holds a BCrypt hash, never plain text.
 */
public class User {

    private int    id;
    private String username;
    private String email;
    private String password; // BCrypt hash

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Used when registering — id is auto-assigned by the DB */
    public User(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    /** Used when fetching from DB */
    public User(int id, String username, String email, String password) {
        this.id       = id;
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
