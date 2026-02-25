package ru.practicum.shareit.request.dto.mapper;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Component
public class ItemRequestMapper {
    public ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto, User requestor) {
        if (itemRequestDto == null) {
            return null;
        }

        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestor(requestor)
                .created(itemRequestDto.getCreated())
                .build();
    }

    public ItemRequestDto mapToDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        Long requestorId = null;
        if (itemRequest.getRequestor() != null) {
            requestorId = itemRequest.getRequestor().getId();
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(requestorId) // безопасно устанавливаем
                .created(itemRequest.getCreated())
                .build();
    }
}
