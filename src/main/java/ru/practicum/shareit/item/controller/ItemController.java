package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @RequestParam(defaultValue = "0") Integer from,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                               @PathVariable Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@RequestHeader(USER_ID_HEADER) Long userId,
                           @PathVariable Long itemId) {
        itemService.deleteItem(userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}
