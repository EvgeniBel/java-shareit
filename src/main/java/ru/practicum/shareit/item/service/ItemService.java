package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

@Service
public interface ItemService {

    List<Item> getItems(long userId);

    Item getItemById(Long itemId);

    Item addNewItem(long userId, Item item);

    void deleteItem(long userId, long itemId);

    Item updateItem(Long userId, Item item);

    List<Item> searchItems(String text);

    Item patchItem(Long userId, Long itemId, Map<String, Object> updates);

    List<Item> getItemsByRequestId(Long requestId);

}
