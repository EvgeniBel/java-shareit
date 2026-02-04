package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getUserItemRequests(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllItemRequests(
    @RequestHeader(USER_ID_HEADER) Long userId,
    @RequestParam(defaultValue = "0") @Min(value = 0, message = "Параметр 'from' не может быть отрицательным") Integer from,
    @RequestParam(defaultValue = "10") @Min(value = 1, message = "Параметр 'size' должен быть положительным") Integer size) {
        return itemRequestService.getAllItemRequests(userId, from, size);
    }


    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getItemRequest(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long requestId) {
        return itemRequestService.getItemRequest(userId, requestId);
    }
}