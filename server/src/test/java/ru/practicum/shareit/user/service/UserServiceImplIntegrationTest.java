package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void testCreateUserValidData() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testCcreateUserDuplicateEmail() {
        UserDto userDto = UserDto.builder()
                .name("Jackson")
                .email("Jackson@example.com")
                .build();
        userService.createUser(userDto);

        UserDto duplicateDto = UserDto.builder()
                .name("Jackson")
                .email("Jackson@example.com")
                .build();

        assertThrows(ConflictException.class, () -> userService.createUser(duplicateDto));
    }

    @Test
    void testGetUserByIdExistingUser() {
        UserDto userDto = UserDto.builder()
                .name("Jackson")
                .email("Jackson@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        UserDto result = userService.getUserById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(created.getName(), result.getName());
        assertEquals(created.getEmail(), result.getEmail());
    }

    @Test
    void testGetUserByIdNonExistingUser() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testGetAllUsers() {
        userService.createUser(UserDto.builder().name("User1").email("user1@test.com").build());
        userService.createUser(UserDto.builder().name("User2").email("user2@test.com").build());

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void testUpdateUserUpdateAllFields() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserUpdateOnlyName() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserUpdateOnlyEmail() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .email("updated@example.com")
                .build();

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertEquals("John Doe", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserDuplicateEmail() {
        UserDto user1 = UserDto.builder().name("User1").email("user1@test.com").build();
        UserDto user2 = UserDto.builder().name("User2").email("user2@test.com").build();

        UserDto created1 = userService.createUser(user1);
        UserDto created2 = userService.createUser(user2);

        UserDto updateDto = UserDto.builder()
                .email(created1.getEmail())
                .build();

        assertThrows(ConflictException.class, () ->
                userService.updateUser(created2.getId(), updateDto));
    }

    @Test
    void testDeleteUserExistingUser() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(created.getId()));
    }

    @Test
    void testDeleteUserNonExistingUser() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));
    }

    @Test
    void testGetUserModelByIdExistingUser() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        UserDto created = userService.createUser(userDto);

        User result = userService.getUserModelById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(created.getName(), result.getName());
        assertEquals(created.getEmail(), result.getEmail());
    }
}