package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Создание запроса вещи пользователем ID={}", userId);

        ItemRequest itemRequest = itemRequestMapper.mapToItemRequest(itemRequestDto);
        itemRequest.setRequestorId(userId);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setId(null);

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос создан с ID={}", savedRequest.getId());

        return itemRequestMapper.mapToDto(savedRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getUserItemRequests(Long userId) {
        log.info("Получение запросов пользователя ID={}", userId);

        return itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(this::buildItemRequestWithItemsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов кроме пользователя ID={}, from={}, size={}", userId, from, size);

        validatePaginationParams(from, size);

        Pageable pageable = createPageable(from, size);

        List<ItemRequest> requests = itemRequestRepository.findAllExceptUser(userId, pageable);

        return requests.stream()
                .map(this::buildItemRequestWithItemsDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestWithItemsDto getItemRequest(Long userId, Long requestId) {
        log.info("Получение запроса ID={} пользователем ID={}", requestId, userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос с ID=%d не найден", requestId)));

        return buildItemRequestWithItemsDto(itemRequest);
    }

    private ItemRequestWithItemsDto buildItemRequestWithItemsDto(ItemRequest itemRequest) {
        List<Item> items = itemService.getItemsByRequestId(itemRequest.getId());

        return ItemRequestWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestorId())
                .created(itemRequest.getCreated())
                .items(items.stream()
                        .map(itemMapper::mapToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private void validatePaginationParams(Integer from, Integer size) {
        if (from == null || size == null) {
            throw new IllegalArgumentException("Параметры пагинации не могут быть null");
        }
        if (from < 0) {
            throw new IllegalArgumentException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Параметр 'size' должен быть положительным");
        }
    }

    private Pageable createPageable(Integer from, Integer size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}