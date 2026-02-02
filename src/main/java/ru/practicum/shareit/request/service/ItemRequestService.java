package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto);
    List<ItemRequestWithItemsDto> getUserItemRequests(Long userId);
    List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);
    ItemRequestWithItemsDto getItemRequest(Long userId, Long requestId);
}
