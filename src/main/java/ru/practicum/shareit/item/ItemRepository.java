package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {

    private final Map<Long, List<Item>> userItems = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Item> findByUserId(Long userId) {
        return userItems.getOrDefault(userId, new ArrayList<>());
    }

    public Optional<Item> findByItemId(Long itemId) {
        return userItems.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        long userId = item.getUserId();
        List<Item> items = userItems.getOrDefault(userId, new ArrayList<>());

        boolean exists = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                exists = true;
                break;
            }
        }

        if (!exists) {
            items.add(item);
        }

        userItems.put(userId, items);
        return item;
    }

    public void deleteByUserIdAndItemId(long userId, long itemId) {
        List<Item> items = userItems.get(userId);
        if (items == null) {
            items.removeIf(item -> item.getId().equals(itemId));
            userItems.put(userId, items);
        }
    }

    public Item updateItem(Item item) {
        return save(item);
    }

    public Optional<Item> getItemById(Long itemId) {
        return userItems.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return userItems.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }
}
