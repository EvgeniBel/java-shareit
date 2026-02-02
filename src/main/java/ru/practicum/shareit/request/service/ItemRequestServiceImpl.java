package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestMapper.mapToItemRequest(itemRequestDto);
        itemRequest.setRequestorId(userId);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.mapToDto(savedRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getUserItemRequests(Long userId) {
        return itemRequestRepository.findByRequestorId(userId).stream()
                .map(this::buildItemRequestWithItemsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Параметр 'size' должен быть положительным");
        }

        List<ItemRequest> allRequests = itemRequestRepository.findAllExceptUser(userId);

        int start = Math.min(from, allRequests.size());
        int end = Math.min(start + size, allRequests.size());

        if (start >= allRequests.size()) {
            return Collections.emptyList();
        }

        List<ItemRequest> paginatedRequests = allRequests.subList(start, end);

        return paginatedRequests.stream()
                .map(itemRequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestWithItemsDto getItemRequest(Long userId, Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return buildItemRequestWithItemsDto(itemRequest);
    }

    private ItemRequestWithItemsDto buildItemRequestWithItemsDto(ItemRequest itemRequest) {
        List<Item> items = itemService.getItemsByRequestId(itemRequest.getId());

        return ItemRequestWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestorId())  // ✅
                .created(itemRequest.getCreated())
                .items(items.stream()
                        .map(itemMapper::mapToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}