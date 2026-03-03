package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemService itemService;

    User requester;
    User anotherUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@test.com")
                .build());

        anotherUser = userRepository.save(User.builder()
                .name("Another")
                .email("another@test.com")
                .build());
    }

    @Test
    void testCreateItemRequestValidData() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .created(LocalDateTime.now())
                .description("Need a drill")
                .build();

        ItemRequestDto result = itemRequestService.createItemRequest(requester.getId(), requestDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(requester.getId(), result.getRequestorId());
    }

    @Test
    void testGetUserItemRequestsWithItems() {
        // Создаем запрос
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
        var createdRequest = itemRequestService.createItemRequest(requester.getId(), requestDto);

        // Создаем вещь в ответ на запрос
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(createdRequest.getId())
                .build();
        itemService.createItem(anotherUser.getId(), itemDto);

        // Получаем запросы
        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(requester.getId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Drill", result.get(0).getItems().get(0).getName());
    }

    @Test
    void testGetAllItemRequestsExcludeUser() {
        ItemRequestDto requestDto1 = ItemRequestDto.builder()
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
        itemRequestService.createItemRequest(requester.getId(), requestDto1);

        ItemRequestDto requestDto2 = ItemRequestDto.builder()
                .description("Need a hammer")
                .created(LocalDateTime.now())
                .build();
        itemRequestService.createItemRequest(anotherUser.getId(), requestDto2);

        List<ItemRequestWithItemsDto> result = itemRequestService.getAllItemRequests(
                requester.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals("Need a hammer", result.get(0).getDescription());
    }

    @Test
    void testGetItemRequestWithItems() {
        // Создаем запрос
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
        var createdRequest = itemRequestService.createItemRequest(requester.getId(), requestDto);

        // Создаем вещь в ответ на запрос
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(createdRequest.getId())
                .build();
        itemService.createItem(anotherUser.getId(), itemDto);

        // Получаем запрос по ID
        ItemRequestWithItemsDto result = itemRequestService.getItemRequest(
                anotherUser.getId(), createdRequest.getId());

        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testGetAllItemRequestsWithPagination() {
        // Создаем несколько запросов
        for (int i = 0; i < 15; i++) {
            ItemRequestDto requestDto = ItemRequestDto.builder()
                    .created(LocalDateTime.now())
                    .description("Request " + i)
                    .build();
            itemRequestService.createItemRequest(anotherUser.getId(), requestDto);
        }

        // Первая страница
        List<ItemRequestWithItemsDto> page1 = itemRequestService.getAllItemRequests(
                requester.getId(), 0, 5);
        assertEquals(5, page1.size());

        // Вторая страница
        List<ItemRequestWithItemsDto> page2 = itemRequestService.getAllItemRequests(
                requester.getId(), 5, 5);
        assertEquals(5, page2.size());

        // Проверяем сортировку (от новых к старым)
        assertTrue(page1.get(0).getCreated().isAfter(page1.get(4).getCreated()));
    }
}
