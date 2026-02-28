package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    Long id;
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    String email;
}
