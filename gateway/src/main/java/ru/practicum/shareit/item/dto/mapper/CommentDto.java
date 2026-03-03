package ru.practicum.shareit.item.dto.mapper;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class CommentDto {
    Long id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    String text;
    String authorName;
    LocalDateTime created;
}
