package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {
    public Item mapToItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .userId(itemDto.getOwner())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
    }

    public ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .owner(item.getUserId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }
}
