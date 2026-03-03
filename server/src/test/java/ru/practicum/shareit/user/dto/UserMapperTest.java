package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void testMapToUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Jackson")
                .email("Jackson@example.com")
                .build();

        User user = userMapper.mapToUser(userDto);

        assertNotNull(user);
        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void testMapToDto() {
        User user = User.builder()
                .id(1L)
                .name("Jackson")
                .email("Jackson@example.com")
                .build();

        UserDto userDto = userMapper.mapToDto(user);

        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void testMapToUserWithNull() {
        User user = userMapper.mapToUser(null);
        assertNull(user);
    }

    @Test
    void testMapToDtoWithNull() {
        UserDto userDto = userMapper.mapToDto(null);
        assertNull(userDto);
    }
}