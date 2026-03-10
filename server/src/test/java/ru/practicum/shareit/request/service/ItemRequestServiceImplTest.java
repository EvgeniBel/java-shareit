package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemService itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Requester")
                .email("requester@example.com")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(1L)
                .userId(2L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(1L)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(1L)
                .build();
    }

    // ==================== CREATE REQUEST TESTS ====================

    @Test
    void testCreateItemRequestWithValidData() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestMapper.mapToItemRequest(itemRequestDto, user)).thenReturn(itemRequest);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(itemRequestMapper.mapToDto(itemRequest)).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createItemRequest(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto, result);
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void testCreateItemRequestWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.createItemRequest(99L, itemRequestDto));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    // ==================== GET USER REQUESTS TESTS ====================

    @Test
    void testGetUserItemRequestsWithItems() {
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemService.getItemsByRequestIds(List.of(1L))).thenReturn(Map.of(1L, List.of(item)));

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());

        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(1L);
        verify(itemService).getItemsByRequestIds(List.of(1L));
    }

    @Test
    void testGetUserItemRequestsWithNoItems() {
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemService.getItemsByRequestIds(List.of(1L))).thenReturn(Map.of(1L, Collections.emptyList()));

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getItems().isEmpty());
    }

    @Test
    void testGetUserItemRequestsWhenNoRequests() {
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(Collections.emptyList());

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemService, never()).getItemsByRequestIds(any());
    }

    @Test
    void testGetUserItemRequestsWithMultipleRequests() {
        ItemRequest secondRequest = ItemRequest.builder()
                .id(2L)
                .description("Need a hammer")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        Item secondItem = Item.builder()
                .id(2L)
                .userId(2L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(2L)
                .build();

        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest, secondRequest));

        Map<Long, List<Item>> itemsByRequestId = Map.of(
                1L, List.of(item),
                2L, List.of(secondItem)
        );
        when(itemService.getItemsByRequestIds(List.of(1L, 2L))).thenReturn(itemsByRequestId);

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemService).getItemsByRequestIds(List.of(1L, 2L));
    }

    // ==================== GET ALL REQUESTS TESTS ====================

    @Test
    void testGetAllItemRequestsWithValidPagination() {
        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));
        when(itemService.getItemsByRequestIds(List.of(1L))).thenReturn(Map.of(1L, List.of(item)));

        List<ItemRequestWithItemsDto> result = itemRequestService.getAllItemRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRequestRepository).findAllExceptUser(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetAllItemRequestsWithEmptyResult() {
        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestWithItemsDto> result = itemRequestService.getAllItemRequests(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemService, never()).getItemsByRequestIds(any());
    }

    @Test
    void testGetAllItemRequestsWithMultipleRequests() {
        ItemRequest secondRequest = ItemRequest.builder()
                .id(2L)
                .description("Need a hammer")
                .requestor(User.builder().id(3L).build())
                .created(LocalDateTime.now())
                .build();

        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest, secondRequest));

        Map<Long, List<Item>> itemsByRequestId = Map.of(
                1L, List.of(item),
                2L, Collections.emptyList()
        );
        when(itemService.getItemsByRequestIds(List.of(1L, 2L))).thenReturn(itemsByRequestId);

        List<ItemRequestWithItemsDto> result = itemRequestService.getAllItemRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== GET SINGLE REQUEST TESTS ====================

    @Test
    void testGetItemRequestWhenRequestExistsWithItems() {
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestWithItemsDto result = itemRequestService.getItemRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testGetItemRequestWhenRequestExistsWithoutItems() {
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemService.getItemsByRequestId(1L)).thenReturn(Collections.emptyList());

        ItemRequestWithItemsDto result = itemRequestService.getItemRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void testGetItemRequestWhenRequestNotFound() {
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequest(1L, 99L));
    }

    // ==================== PAGINATION VALIDATION TESTS ====================

    @Test
    void testGetAllItemRequestsWithNullFrom() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllItemRequests(1L, null, 10));
        verify(itemRequestRepository, never()).findAllExceptUser(any(), any());
    }

    @Test
    void testGetAllItemRequestsWithNullSize() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllItemRequests(1L, 0, null));
        verify(itemRequestRepository, never()).findAllExceptUser(any(), any());
    }

    @Test
    void testGetAllItemRequestsWithNegativeFrom() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllItemRequests(1L, -1, 10));
        verify(itemRequestRepository, never()).findAllExceptUser(any(), any());
    }

    @Test
    void testGetAllItemRequestsWithZeroSize() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllItemRequests(1L, 0, 0));
        verify(itemRequestRepository, never()).findAllExceptUser(any(), any());
    }

    @Test
    void testGetAllItemRequestsWithNegativeSize() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getAllItemRequests(1L, 0, -5));
        verify(itemRequestRepository, never()).findAllExceptUser(any(), any());
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    void testGetAllItemRequestsWithLargeFrom() {
        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // from=100, size=10 -> page=10
        itemRequestService.getAllItemRequests(1L, 100, 10);

        verify(itemRequestRepository).findAllExceptUser(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetAllItemRequestsWithNonDivisibleFrom() {
        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // from=15, size=10 -> page=1 (целочисленное деление)
        itemRequestService.getAllItemRequests(1L, 15, 10);

        verify(itemRequestRepository).findAllExceptUser(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetUserItemRequestsWhenUserHasNoRequests() {
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(Collections.emptyList());

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== INTEGRATION OF PRIVATE METHODS TESTS ====================

    @Test
    void testCreatePageableWithValidParams() {
        // Косвенно тестируем через публичный метод
        when(itemRequestRepository.findAllExceptUser(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        itemRequestService.getAllItemRequests(1L, 20, 5); // from/size = 4

        verify(itemRequestRepository).findAllExceptUser(eq(1L), argThat(pageable ->
                pageable.getPageNumber() == 4 && pageable.getPageSize() == 5));
    }

    @Test
    void testBuildItemRequestWithItemsDto() {
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestWithItemsDto result = itemRequestService.getItemRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getRequestor().getId(), result.getRequestorId());
        assertEquals(itemRequest.getCreated(), result.getCreated());
    }

    @Test
    void testEnrichRequestsWithItemsWhenItemsNotFound() {
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        when(itemService.getItemsByRequestIds(List.of(1L))).thenReturn(Map.of(1L, Collections.emptyList()));

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserItemRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getItems().isEmpty());
    }
}