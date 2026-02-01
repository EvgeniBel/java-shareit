package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public List<Item> getItems(long userId) {
        return itemRepository.findByUserId(userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item с ID=%d не найден", itemId)));
    }

    @Override
    public Item addNewItem(long userId, Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Имя вещи не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }

        item.setUserId(userId);
        return itemRepository.save(item);
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        Item existingItem = getItemById(itemId);
        if (!existingItem.getUserId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Вещь с ID=%d не найдена у пользователя с ID=%d", itemId, userId)
            );
        }
        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    public Item updateItem(Long userId, Item item) {
        Item existingItem = getItemById(item.getId());
        // Проверяем, что пользователь владеет вещью
        if (!existingItem.getUserId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Вещь с ID=%d не найдена у пользователя с ID=%d", item.getId(), userId)
            );
        }

        if (item.getName() != null && !item.getName().isBlank()) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        return itemRepository.save(existingItem);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemRepository.searchItems(text.trim());
    }

}
