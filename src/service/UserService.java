package service;

import model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public User register(String username, String password, String displayName) {
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return null;
            }
        }
        User user = new User(idCounter.getAndIncrement(), username, password, displayName, null, null);
        users.put(user.getId(), user);
        return user;
    }

    public User login(String username, String password) {
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public void createSession(String token, long userId) {
        sessions.put(token, userId);
    }

    public Long getSessionUser(String token) {
        return sessions.get(token);
    }

    public void removeSession(String token) {
        sessions.remove(token);
    }

    public User getUserById(long id) {
        return users.get(id);
    }

    public User getUserByUsername(String username) {
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public long getUserCount() {
        return users.size();
    }
}
