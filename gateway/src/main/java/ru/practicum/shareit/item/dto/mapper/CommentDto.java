package ru.practicum.shareit.item.dto.mapper;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class CommentDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;
}
