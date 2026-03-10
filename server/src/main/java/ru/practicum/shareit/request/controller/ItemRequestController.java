package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Server: POST /requests for user {} with description: {}", userId, itemRequestDto.getDescription());

        itemRequestDto.setRequestorId(userId);

        if (itemRequestDto.getCreated() == null) {
            itemRequestDto.setCreated(java.time.LocalDateTime.now());
        }

        return itemRequestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getUserItemRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Server: GET /requests for user {}", userId);
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllItemRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                            @RequestParam(defaultValue = "0") Integer from,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /requests/all for user {} with from={}, size={}", userId, from, size);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getItemRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @PathVariable Long requestId) {
        log.info("Server: GET /requests/{} for user {}", requestId, userId);
        return itemRequestService.getItemRequest(userId, requestId);
    }
}