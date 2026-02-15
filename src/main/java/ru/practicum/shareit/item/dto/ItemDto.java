package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class ItemDto {
    Long id;
    Long owner;
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank(message = "Описание не может быть пустым")
    String description;
    @NotNull(message = "Поле available обязательно")
    Boolean available;
    Long requestId;

    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
    List<CommentDto> comments;
}
