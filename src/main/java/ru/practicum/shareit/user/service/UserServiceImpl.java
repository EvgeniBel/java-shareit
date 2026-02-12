package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");

        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя с ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User с Id=%d не найден", userId)));

        return userMapper.mapToDto(user);
    }

    @Override
    public User getUserModelById(Long userId) {
        log.info("Получение модели пользователя с ID={}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User с Id=%d не найден", userId)));
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException(
                    String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }

        User user = userMapper.mapToUser(userDto);
        user.setId(null); // гарантируем создание новой записи

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с ID={}", savedUser.getId());

        return userMapper.mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID=%d не найден", userId)));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new DuplicateEmailException(
                        String.format("Email %s уже используется", userDto.getEmail()));
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID={} обновлен", userId);

        return userMapper.mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    String.format("Пользователь с ID=%d не найден", userId));
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с ID={} удален", userId);
    }
}