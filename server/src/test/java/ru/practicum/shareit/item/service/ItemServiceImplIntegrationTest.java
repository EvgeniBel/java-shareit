package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplIntegrationTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    ItemRequestService itemRequestService;

    User owner;
    User booker;
    Item item;
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@test.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@test.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .userId(owner.getId())
                .build());
    }

    @Test
    void testCreateItemValidData() {
        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        ItemDto result = itemService.createItem(owner.getId(), itemDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Hammer", result.getName());
        assertEquals(owner.getId(), result.getOwner());
    }

    @Test
    void testCreateItemWithRequestId() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a hammer")
                .created(LocalDateTime.now())  // Явно указываем дату
                .build();

        var createdRequest = itemRequestService.createItemRequest(booker.getId(), requestDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(createdRequest.getId())
                .build();

        ItemDto result = itemService.createItem(owner.getId(), itemDto);

        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getRequestId());
    }

    @Test
    void testGetItemByIdAsOwner() {
        ItemDto result = itemService.getItemById(owner.getId(), item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertNotNull(result.getComments());
    }

    @Test
    void testUpdateItemUpdateAllFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertEquals("Updated Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(false, result.getAvailable());
    }

    @Test
    void testSearchItemsWithValidText() {
        List<ItemDto> result = itemService.searchItems("drill", 0, 10);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void testGetUserItemsWithPagination() {
        // Добавим еще одну вещь
        Item item2 = itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .userId(owner.getId())
                .build());

        List<ItemDto> result = itemService.getUserItems(owner.getId(), 0, 1);

        assertEquals(1, result.size());
    }

    @Test
    void testGetItemsByRequestIdsWithValidIds() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        var request = itemRequestService.createItemRequest(booker.getId(), requestDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(owner.getId(), itemDto);

        var result = itemService.getItemsByRequestIds(List.of(request.getId()));

        assertFalse(result.isEmpty());
        assertEquals(1, result.get(request.getId()).size());
    }
}
