package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public List<User> getAllUsers() {
        return repository.getAllUser();
    }

    public User getUserById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format("User с Id=%d не найден", userId)));
    }

    public User createUser(User user) {
        return repository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        return repository.save(existingUser);
    }

    public void deleteUser(Long userId) {
        repository.deleteUser(userId);
    }
}
