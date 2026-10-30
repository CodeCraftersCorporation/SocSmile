package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Post {
    private long id;
    private long authorId;
    private String authorName;
    private String authorAvatar;
    private String text;
    private String imageUrl;
    private long[] likeIds;
    private String createdAt;

    public Post() {}

    public Post(long id, long authorId, String text, String imageUrl) {
        this.id = id;
        this.authorId = authorId;
        this.text = text;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.likeIds = new long[0];
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long[] getLikeIds() { return likeIds; }
    public void setLikeIds(long[] likeIds) { this.likeIds = likeIds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getLikeCount() { return likeIds.length; }

    public boolean hasLiked(long userId) {
        for (long likeId : likeIds) {
            if (likeId == userId) return true;
        }
        return false;
    }

    public void setLiked(long userId) {
        this.liked = hasLiked(userId);
    }

    public boolean isLiked() {
        return liked;
    }

    private boolean liked = false;

    public void addLike(long userId) {
        long[] newLikes = Arrays.copyOf(likeIds, likeIds.length + 1);
        newLikes[newLikes.length - 1] = userId;
        this.likeIds = newLikes;
    }

    public void removeLike(long userId) {
        long[] newLikes = new long[likeIds.length - 1];
        int idx = 0;
        for (long likeId : likeIds) {
            if (likeId != userId) {
                newLikes[idx++] = likeId;
            }
        }
        this.likeIds = newLikes;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"id\":").append(id).append(",")
          .append("\"authorId\":").append(authorId).append(",")
          .append("\"authorName\":\"").append(escape(authorName)).append("\",")
          .append("\"authorAvatar\":\"").append(escape(authorAvatar)).append("\",")
          .append("\"text\":\"").append(escape(text)).append("\",")
          .append("\"imageUrl\":\"").append(escape(imageUrl)).append("\",")
          .append("\"likeCount\":").append(likeIds.length).append(",")
          .append("\"liked\":").append(liked).append(",")
          .append("\"createdAt\":\"").append(createdAt).append("\"")
          .append(",");

        sb.append("\"likeIds\":[");
        for (int i = 0; i < likeIds.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(likeIds[i]);
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
