package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

@Service
public interface ItemService {

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto getItemById(Long userId, Long itemId);

    void deleteItem(Long userId, Long itemId);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchItems(String text, Integer from, Integer size);

    List<Item> getItemsByRequestId(Long requestId);

    List<ItemDto> getUserItems(Long userId, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

    List<ItemDto> getItemsByOwner(Long ownerId);
}
