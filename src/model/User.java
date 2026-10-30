package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private long id;
    private String username;
    private String password;
    private String displayName;
    private String avatar;
    private String bio;
    private String createdAt;

    public User() {}

    public User(long id, String username, String password, String displayName, String avatar, String bio) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.displayName = displayName != null ? displayName : username;
        this.avatar = avatar != null ? avatar : "https://ui-avatars.com/api/?name=" + username + "&background=6c5ce7&color=fff";
        this.bio = bio != null ? bio : "";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String toJson() {
        return "{"
            + "\"id\":" + id + ","
            + "\"username\":\"" + escape(username) + "\","
            + "\"displayName\":\"" + escape(displayName) + "\","
            + "\"avatar\":\"" + escape(avatar) + "\","
            + "\"bio\":\"" + escape(bio) + "\","
            + "\"createdAt\":\"" + createdAt + "\""
            + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:   sb.append(c); break;
            }
        }
        return sb.toString();
    }
}
