package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailToIdMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }

        if (users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            if (!existingUser.getEmail().equals(user.getEmail())) {
                emailToIdMap.remove(existingUser.getEmail());
            }
        }

        users.put(user.getId(), user);
        emailToIdMap.put(user.getEmail(), user.getId());
        return user;
    }

    public void deleteUser(Long userId) {
        User user = users.get(userId);
        if (user != null) {
            emailToIdMap.remove(user.getEmail());
            users.remove(userId);
        }
    }

    public Optional<User> findByEmail(String email) {
        Long userId = emailToIdMap.get(email);
        return userId != null ? Optional.ofNullable(users.get(userId)) : Optional.empty();
    }
}
