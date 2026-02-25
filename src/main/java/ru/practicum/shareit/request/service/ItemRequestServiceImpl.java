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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Создание запроса вещи пользователем ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID=%d не найден", userId)));

        ItemRequest itemRequest = itemRequestMapper.mapToItemRequest(itemRequestDto, user);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос создан с ID={}", savedRequest.getId());

        return itemRequestMapper.mapToDto(savedRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getUserItemRequests(Long userId) {
        log.info("Получение запросов пользователя ID={}", userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов кроме пользователя ID={}, from={}, size={}", userId, from, size);

        validatePaginationParams(from, size);

        Pageable pageable = createPageable(from, size);

        List<ItemRequest> requests = itemRequestRepository.findAllExceptUser(userId, pageable);

        return enrichRequestsWithItems(requests);
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
        return buildItemRequestWithItemsDto(itemRequest, items);
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

    private List<ItemRequestWithItemsDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Собираем ID всех запросов
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemService.getItemsByRequestIds(requestIds);

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return buildItemRequestWithItemsDto(request, items);
                })
                .collect(Collectors.toList());
    }

    private ItemRequestWithItemsDto buildItemRequestWithItemsDto(ItemRequest request, List<Item> items) {
        return ItemRequestWithItemsDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .items(items.stream()
                        .map(itemMapper::mapToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}