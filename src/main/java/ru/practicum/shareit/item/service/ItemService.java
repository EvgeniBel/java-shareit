package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
public interface ItemService {

    List<Item> getItems(long userId);

    Item getItemById(Long itemId);

    Item addNewItem(long userId, Item item);

    void deleteItem(long userId, long itemId);

    Item updateItem(Item item);

    List<Item> searchItems(String text);
}
