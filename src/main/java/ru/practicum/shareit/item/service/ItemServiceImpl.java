package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Override
    public List<Item> getItems(long userId) {
        userService.getUserById(userId);
        return itemRepository.findByUserId(userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item с ID=%d не найден", itemId)));
    }

    @Override
    @Transactional
    public Item addNewItem(long userId, Item item) {
        userService.getUserById(userId);
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Имя вещи не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }
        if (item.getRequestId() != null) {
            var request = itemRequestRepository.findById(item.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        item.setUserId(userId);
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteItem(long userId, long itemId) {
        userService.getUserById(userId);
        Item existingItem = getItemByIdAndCheckOwner(itemId, userId);
        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    @Transactional
    public Item updateItem(Long userId, Item item) {
        Item existingItem = getItemByIdAndCheckOwner(item.getId(), userId);

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
    @Transactional
    public List<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemRepository.searchItems(text.trim());
    }

    @Override
    @Transactional
    public Item patchItem(Long userId, Long itemId, Map<String, Object> updates) {
        userService.getUserById(userId);
        Item existingItem = getItemByIdAndCheckOwner(itemId, userId);

        if (updates.containsKey("name") && updates.get("name") != null) {
            existingItem.setName((String) updates.get("name"));
        }
        if (updates.containsKey("description") && updates.get("description") != null) {
            existingItem.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("available") && updates.get("available") != null) {
            existingItem.setAvailable((Boolean) updates.get("available"));
        }

        return itemRepository.save(existingItem);
    }

    @Override
    @Transactional
    public List<Item> getItemsByRequestId(Long requestId) {
        if (requestId != null) {
            itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        return itemRepository.findByRequestId(requestId);
    }

    private Item getItemByIdAndCheckOwner(Long itemId, Long userId) {
        Item item = getItemById(itemId);
        if (!item.getUserId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Вещь с ID=%d не найдена у пользователя с ID=%d", itemId, userId)
            );
        }
        return item;
    }
}
