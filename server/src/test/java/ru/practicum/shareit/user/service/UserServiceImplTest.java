package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private final Long userId = 1L;
    private final Long nonExistentId = 99L;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .name("Jakson")
                .email("email@example.com")
                .build();

        userDto = UserDto.builder()
                .id(userId)
                .name("Jakson")
                .email("email@example.com")
                .build();
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.mapToDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).mapToDto(user);
    }

    @Test
    void testGetAllUsersWhenEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, never()).mapToDto(any());
    }

    @Test
    void testGetUserByIdWhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userDto, result);
        assertEquals(userId, result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).mapToDto(user);
    }

    @Test
    void testGetUserByIdWhenUserNotFound() {
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(nonExistentId));

        assertEquals(String.format("User с Id=%d не найден", nonExistentId), exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userMapper, never()).mapToDto(any());
    }

    @Test
    void testGetUserModelByIdWhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserModelById(userId);

        assertNotNull(result);
        assertEquals(user, result);
        assertEquals(userId, result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).mapToDto(any());
    }

    @Test
    void testGetUserModelByIdWhenUserNotFound() {
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserModelById(nonExistentId));

        assertEquals(String.format("User с Id=%d не найден", nonExistentId), exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testCreateUserWithValidData() {
        User newUser = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        User savedUser = User.builder()
                .id(userId)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userMapper.mapToUser(userDto)).thenReturn(newUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.mapToDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto, result);
        assertEquals(userId, result.getId());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userMapper, times(1)).mapToUser(userDto);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(userMapper, times(1)).mapToDto(savedUser);

        User capturedUser = userCaptor.getValue();
        assertNull(capturedUser.getId()); // Проверяем, что ID был установлен в null
        assertEquals(userDto.getName(), capturedUser.getName());
        assertEquals(userDto.getEmail(), capturedUser.getEmail());
    }

    @Test
    void testCreateUserWithExistingEmail() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(userDto));

        assertEquals(String.format("Пользователь с email %s уже существует", userDto.getEmail()),
                exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userMapper, never()).mapToUser(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).mapToDto(any());
    }

    @Test
    void testCreateUser() {
        User userWithId = User.builder()
                .id(999L)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userMapper.mapToUser(userDto)).thenReturn(userWithId);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDto(user)).thenReturn(userDto);

        userService.createUser(userDto);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getId(), "ID должен быть null при создании нового пользователя");
    }

    @Test
    void testUpdateUserWithNameAndEmail() {
        UserDto updateDto = UserDto.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("UpdatedName")
                .email("updated@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(updateDto.getEmail(), userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.mapToDto(updatedUser)).thenReturn(updateDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("updated@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmailAndIdNot(updateDto.getEmail(), userId);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("UpdatedName", capturedUser.getName());
        assertEquals("updated@example.com", capturedUser.getEmail());
    }

    @Test
    void testUpdateUserWithOnlyName() {
        UserDto updateDto = UserDto.builder()
                .name("UpdatedName")
                .build();

        User expectedUser = User.builder()
                .id(userId)
                .name("UpdatedName")
                .email(user.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(userMapper.mapToDto(expectedUser)).thenReturn(
                UserDto.builder().name("UpdatedName").email(user.getEmail()).build());

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("UpdatedName", capturedUser.getName());
        assertEquals(user.getEmail(), capturedUser.getEmail());
    }

    @Test
    void testUpdateUserWithOnlyEmail() {
        UserDto updateDto = UserDto.builder()
                .email("newemail@example.com")
                .build();

        User expectedUser = User.builder()
                .id(userId)
                .name(user.getName())
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(updateDto.getEmail(), userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(userMapper.mapToDto(expectedUser)).thenReturn(
                UserDto.builder().name(user.getName()).email("newemail@example.com").build());

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(user.getName(), result.getName());
        assertEquals("newemail@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmailAndIdNot(updateDto.getEmail(), userId);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(user.getName(), capturedUser.getName());
        assertEquals("newemail@example.com", capturedUser.getEmail());
    }

    @Test
    void testUpdateUserWithSameEmail() {
        UserDto updateDto = UserDto.builder()
                .email(user.getEmail())
                .name("UpdatedName")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDto(any(User.class))).thenReturn(updateDto);

        userService.updateUser(userId, updateDto);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserWhenUserNotFound() {
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(nonExistentId, userDto));

        assertEquals(String.format("Пользователь с ID=%d не найден", nonExistentId),
                exception.getMessage());

        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserWithExistingEmail() {
        UserDto updateDto = UserDto.builder()
                .email("existing@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(updateDto.getEmail(), userId)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(userId, updateDto));

        assertEquals(String.format("Email %s уже используется", updateDto.getEmail()),
                exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmailAndIdNot(updateDto.getEmail(), userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUserWhenUserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUserWhenUserNotFound() {
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(nonExistentId));

        assertEquals(String.format("Пользователь с ID=%d не найден", nonExistentId),
                exception.getMessage());

        verify(userRepository, times(1)).existsById(nonExistentId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testUpdateUserWithPartialUpdate() {
        UserDto updateDto = UserDto.builder()
                .name("UpdatedName")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.mapToDto(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return UserDto.builder().name(u.getName()).email(u.getEmail()).build();
        });

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals(user.getEmail(), result.getEmail()); // Email должен сохраниться

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(user.getId(), savedUser.getId());
        assertEquals("UpdatedName", savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }
}