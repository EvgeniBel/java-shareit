package ru.practicum.shareit.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@Component
public class UserMapper {
    public User mapToUser(UserDto userDto) {
        return Optional.ofNullable(userDto)
                .map(dto -> {
                    User user = new User();
                    user.setId(dto.getId());
                    user.setName(dto.getName());
                    user.setEmail(dto.getEmail());
                    return user;
                })
                .orElse(null);

    }

    public UserDto mapToDto(User user) {

        return Optional.ofNullable(user)
                .map(u -> {
                    UserDto dto = new UserDto();
                    dto.setId(u.getId());
                    dto.setName(u.getName());
                    dto.setEmail(u.getEmail());

                    return dto;
                })
                .orElse(null);
    }
}
