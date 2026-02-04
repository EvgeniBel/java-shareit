package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    Long id;
    Long userId;
    String name;
    String description;
    Boolean available;
    Long requestId;
}
