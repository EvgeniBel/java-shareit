package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestMapper.mapToItemRequest(itemRequestDto);
        itemRequest.setRequestorId(userId);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.mapToDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(userId);
        return requests.stream()
                .map(itemRequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        // TODO: реализовать пагинацию
        List<ItemRequest> requests = itemRequestRepository.findAllExceptUser(userId);
        return requests.stream()
                .map(itemRequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequest(Long userId, Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return itemRequestMapper.mapToDto(itemRequest);
    }

    public ItemRequestWithItemsDto getRequestByIdWithItems(Long userId, Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemService.getItemsByRequestId(requestId);

        ItemRequestWithItemsDto dto = mapToWithItemsDto(itemRequest);
        dto.setItems(items.stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private ItemRequestWithItemsDto mapToWithItemsDto(ItemRequest itemRequest) {
        return ItemRequestWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestorId())
                .created(itemRequest.getCreated())
                .build();
    }
}