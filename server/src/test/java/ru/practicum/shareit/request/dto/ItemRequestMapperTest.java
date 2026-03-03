package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private final LocalDateTime now = LocalDateTime.now();
    private ItemRequestMapper itemRequestMapper;
    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        itemRequestMapper = new ItemRequestMapper();

        user = User.builder()
                .id(1L)
                .name("Requester")
                .email("requester@example.com")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(now)
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(now)
                .build();
    }

    @Test
    void testMapToItemRequest() {
        ItemRequest result = itemRequestMapper.mapToItemRequest(itemRequestDto, user);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(user, result.getRequestor());
        assertEquals(itemRequestDto.getCreated(), result.getCreated());
    }

    @Test
    void testMapToItemRequestWithNullDto() {
        ItemRequest result = itemRequestMapper.mapToItemRequest(null, user);
        assertNull(result);
    }

    @Test
    void testMapToDto() {
        ItemRequestDto result = itemRequestMapper.mapToDto(itemRequest);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getRequestor().getId(), result.getRequestorId());
        assertEquals(itemRequest.getCreated(), result.getCreated());
    }

    @Test
    void testMapToDtoWithNullEntity() {
        ItemRequestDto result = itemRequestMapper.mapToDto(null);
        assertNull(result);
    }

    @Test
    void testMapToDtoWithNullRequestor() {
        ItemRequest requestWithNullRequestor = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(null)
                .created(now)
                .build();

        ItemRequestDto result = itemRequestMapper.mapToDto(requestWithNullRequestor);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertNull(result.getRequestorId());
        assertEquals(now, result.getCreated());
    }

    @Test
    void testMapToItemRequestSetAllFieldsCorrectly() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(10L)
                .description("New request")
                .created(now)
                .build();

        ItemRequest result = itemRequestMapper.mapToItemRequest(dto, user);

        assertEquals(10L, result.getId());
        assertEquals("New request", result.getDescription());
        assertEquals(user, result.getRequestor());
        assertEquals(now, result.getCreated());
    }
}