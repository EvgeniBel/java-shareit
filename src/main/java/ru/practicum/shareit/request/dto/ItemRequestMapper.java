package ru.practicum.shareit.request.dto;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.ItemRequest;

@Component
public class ItemRequestMapper {
    public ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestorId(itemRequestDto.getRequestorId())
                .created(itemRequestDto.getCreated())
                .build();
    }

    public ItemRequestDto mapToDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestorId())
                .created(itemRequest.getCreated())
                .build();
    }
}
