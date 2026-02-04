package ru.practicum.shareit.request.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    Long id;
    String description;
    Long requestorId;
    LocalDateTime created;
}
