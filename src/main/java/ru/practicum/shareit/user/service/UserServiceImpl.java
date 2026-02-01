package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public User getUserById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User с Id=%d не найден", userId)));
    }

    @Override
    public User createUser(User user) {
        repository.findByEmail(user.getEmail())
                .ifPresent(existingUser -> {
                    throw new ConflictException(
                            String.format("Пользователь с email '%s' уже существует", user.getEmail())
                    );
                });

        return repository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        // Проверяем, меняется ли email
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String newEmail = user.getEmail();

            if (!newEmail.equals(existingUser.getEmail())) {
                // Проверяем, не занят ли новый email другим пользователем
                repository.findByEmail(newEmail)
                        .ifPresent(userWithSameEmail -> {
                            if (!userWithSameEmail.getId().equals(id)) {
                                throw new ConflictException(
                                        String.format("Email '%s' уже используется другим пользователем", newEmail)
                                );
                            }
                        });
                existingUser.setEmail(newEmail);
            }
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        }

        return repository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        repository.deleteUser(userId);
    }
}
