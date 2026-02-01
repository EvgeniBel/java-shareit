package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Later-User-Id") long userId) {
        return itemService.getItems(userId).stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        return itemMapper.mapToDto(itemService.getItemById(itemId));
    }

    @PostMapping
    public ItemDto addNewItem(@RequestHeader("X-Later-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        var item = itemMapper.mapToItem(itemDto);
        var savedItem = itemService.addNewItem(userId, item);
        return itemMapper.mapToDto(savedItem);
    }

    @DeleteMapping
    public void delete(@RequestHeader("X-Later-User-Id") long userId,
                       @PathVariable(name = "itemId") long itemId) {
        itemService.deleteItem(userId, itemId);
    }

    @PutMapping
    public ItemDto updateItem(@PathVariable Long itemId,
                              @Valid @RequestBody ItemDto itemDto) {
        var item = itemMapper.mapToItem(itemDto);
        item.setId(itemId);
        var updatedItem = itemService.updateItem(item);
        return itemMapper.mapToDto(updatedItem);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text).stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
