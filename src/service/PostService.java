package service;

import model.Post;
import model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PostService {
    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private final UserService userService;
    private final AtomicLong idCounter = new AtomicLong(1);

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Post createPost(long authorId, String text, String imageUrl) {
        User author = userService.getUserById(authorId);
        if (author == null) return null;

        Post post = new Post(idCounter.getAndIncrement(), authorId, text, imageUrl);
        post.setAuthorName(author.getDisplayName());
        post.setAuthorAvatar(author.getAvatar());
        posts.put(post.getId(), post);
        return post;
    }

    public List<Post> getAllPosts() {
        List<Post> list = new ArrayList<>(posts.values());
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return list;
    }

    public Post getPostById(long id) {
        return posts.get(id);
    }

    public List<Post> getPostsByUser(long userId) {
        List<Post> list = new ArrayList<>();
        for (Post p : posts.values()) {
            if (p.getAuthorId() == userId) {
                list.add(p);
            }
        }
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return list;
    }

    public void addLike(long postId, long userId) {
        Post post = posts.get(postId);
        if (post != null) {
            post.addLike(userId);
        }
    }

    public void removeLike(long postId, long userId) {
        Post post = posts.get(postId);
        if (post != null) {
            post.removeLike(userId);
        }
    }

    public boolean hasLiked(long postId, long userId) {
        Post post = posts.get(postId);
        return post != null && post.hasLiked(userId);
    }

    public int getPostCount() {
        return posts.size();
    }
}
