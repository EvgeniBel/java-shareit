package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    UserService userService;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingMapper bookingMapper;

    @InjectMocks
    ItemServiceImpl itemService;

    User user;
    User booker;
    Item item;
    ItemDto itemDto;
    Comment comment;
    CommentDto commentDto;
    ItemRequest itemRequest;
    Booking lastBooking;
    Booking nextBooking;
    BookingShortDto lastBookingShortDto;
    BookingShortDto nextBookingShortDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .userId(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(null)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .owner(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item");
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item");
        commentDto.setAuthorName("Owner");
        commentDto.setCreated(LocalDateTime.now());

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        lastBookingShortDto = new BookingShortDto();
        lastBookingShortDto.setId(1L);
        lastBookingShortDto.setBookerId(2L);

        nextBookingShortDto = new BookingShortDto();
        nextBookingShortDto.setId(2L);
        nextBookingShortDto.setBookerId(2L);
    }

    // ==================== ТЕСТЫ CREATE ITEM ====================

    @Test
    void testCreateItemValidData() {
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertNotNull(result);
        assertEquals(itemDto, result);
        verify(itemRepository).save(any(Item.class));
        verify(userService).getUserById(1L);
    }

    @Test
    void testCreateItemWithRequestId() {
        item.setRequestId(1L);
        itemDto.setRequestId(1L);

        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertNotNull(result);
        assertEquals(itemDto, result);
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void testCreateItemWithInvalidRequestId() {
        item.setRequestId(99L);
        itemDto.setRequestId(99L);

        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testCreateItemWithNullName() {
        item.setName(null);
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testCreateItemWithBlankName() {
        item.setName("   ");
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testCreateItemWithNullDescription() {
        item.setDescription(null);
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testCreateItemWithBlankDescription() {
        item.setDescription("   ");
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void tetsCreateItemWithNullAvailable() {
        item.setAvailable(null);
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemMapper.mapToItem(itemDto)).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    // ==================== ТЕСТЫ GET ITEM BY ID ====================

    @Test
    void testGetItemByIdAsNonOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(2L, 1L);

        assertNotNull(result);
        assertEquals(itemDto, result);
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());

        verify(bookingRepository, never()).findFirstByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any());
        verify(bookingRepository, never()).findFirstByItemIdAndStartAfterOrderByStartAsc(anyLong(), any());
    }

    @Test
    void testGetItemByIdAsOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));
        when(bookingMapper.mapToShortDto(lastBooking)).thenReturn(lastBookingShortDto);
        when(bookingMapper.mapToShortDto(nextBooking)).thenReturn(nextBookingShortDto);

        ItemDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        assertEquals(itemDto, result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());

        verify(bookingRepository).findFirstByItemIdAndEndBeforeOrderByEndDesc(eq(1L), any());
        verify(bookingRepository).findFirstByItemIdAndStartAfterOrderByStartAsc(eq(1L), any());
    }

    @Test
    void testGetItemByIdAsOwnerNoBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ItemDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        assertEquals(itemDto, result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void testGetItemByIdWithNoComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getItemById(2L, 1L);

        assertNotNull(result);
        assertEquals(itemDto, result);
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void testGetItemByIdItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 99L));
    }

    // ==================== ТЕСТЫ GET ITEMS BY REQUEST IDS ====================

    @Test
    void testGetItemsByRequestIdsWithValidRequestIds() {
        List<Long> requestIds = List.of(1L, 2L);
        Item item1 = Item.builder().id(1L).requestId(1L).build();
        Item item2 = Item.builder().id(2L).requestId(1L).build();
        Item item3 = Item.builder().id(3L).requestId(2L).build();

        when(itemRepository.findByRequestIdIn(requestIds)).thenReturn(List.of(item1, item2, item3));

        Map<Long, List<Item>> result = itemService.getItemsByRequestIds(requestIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(1L).size());
        assertEquals(1, result.get(2L).size());
    }

    @Test
    void testGetItemsByRequestIdsWithNullRequestIds() {
        Map<Long, List<Item>> result = itemService.getItemsByRequestIds(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByRequestIdIn(any());
    }

    @Test
    void testGetItemsByRequestIdsWithEmptyList() {
        Map<Long, List<Item>> result = itemService.getItemsByRequestIds(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByRequestIdIn(any());
    }

    @Test
    void testGetItemsByRequestIdsNoItemsFound() {
        List<Long> requestIds = List.of(1L, 2L);
        when(itemRepository.findByRequestIdIn(requestIds)).thenReturn(Collections.emptyList());

        Map<Long, List<Item>> result = itemService.getItemsByRequestIds(requestIds);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== ТЕСТЫ DELETE ITEM ====================

    @Test
    void testDeleteItemAsOwner() {
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(any(Item.class));

        itemService.deleteItem(1L, 1L);

        verify(userService).getUserById(1L);
        verify(itemRepository).findById(1L);
        verify(itemRepository).delete(item);
    }

    @Test
    void testDeleteItemItemNotFound() {
        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.deleteItem(1L, 99L));
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void testDeleteItemNotOwner() {
        Item item2 = Item.builder().id(1L).userId(2L).build();

        when(userService.getUserById(1L)).thenReturn(UserDto.builder().build());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item2));

        assertThrows(NotFoundException.class, () -> itemService.deleteItem(1L, 1L));
        verify(itemRepository, never()).delete(any(Item.class));
    }

    // ==================== ТЕСТЫ UPDATE ITEM ====================

    @Test
    void testUpdateItemUpdateAllFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Drill", item.getName());
        assertEquals("Updated description", item.getDescription());
        assertEquals(false, item.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void tesUpdateItemUpdateOnlyName() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Drill", item.getName());
        assertEquals("Electric drill", item.getDescription());
        assertEquals(true, item.getAvailable());
    }

    @Test
    void testUpdateItemUpdateOnlyDescription() {
        ItemDto updateDto = ItemDto.builder()
                .description("Updated description")
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Drill", item.getName());
        assertEquals("Updated description", item.getDescription());
        assertEquals(true, item.getAvailable());
    }

    @Test
    void testUpdateItemUpdateOnlyAvailable() {
        ItemDto updateDto = ItemDto.builder()
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Drill", item.getName());
        assertEquals("Electric drill", item.getDescription());
        assertEquals(false, item.getAvailable());
    }

    @Test
    void testUpdateItemItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 99L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testUpdateItemUserIsNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(UnauthorizedAccessException.class, () -> itemService.updateItem(2L, 1L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    // ==================== ТЕСТЫ SEARCH ITEMS ====================

    @Test
    void testSearchItemsWithValidText() {
        when(itemRepository.search(eq("drill"), any(Pageable.class))).thenReturn(List.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("drill", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRepository).search(eq("drill"), any(Pageable.class));
    }

    @Test
    void tetsSearchItemsWithNullText() {
        List<ItemDto> result = itemService.searchItems(null, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString(), any());
    }

    @Test
    void testSearchItemsWithBlankText() {
        List<ItemDto> result = itemService.searchItems("   ", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString(), any());
    }

    @Test
    void testSearchItemsNoResults() {
        when(itemRepository.search(eq("nonexistent"), any(Pageable.class))).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.searchItems("nonexistent", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchItemsWithPaginationCallsRepository() {
        itemService.searchItems("drill", 10, 5);
        verify(itemRepository).search(eq("drill"), any(Pageable.class));
    }

    // ==================== ТЕСТЫ GET ITEMS BY REQUEST ID ====================

    @Test
    void testGetItemsByRequestIdWithValidRequestId() {
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<Item> result = itemService.getItemsByRequestId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRequestRepository).findById(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void testGetItemsByRequestIdWithNullRequestId() {
        when(itemRepository.findByRequestId(null)).thenReturn(Collections.emptyList());

        List<Item> result = itemService.getItemsByRequestId(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void testGetItemsByRequestIdRequestNotFound() {
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemsByRequestId(99L));
        verify(itemRepository, never()).findByRequestId(any());
    }

    // ==================== ТЕСТЫ GET USER ITEMS ====================

    @Test
    void testGetUserItemsWithValidPagination() {
        when(itemRepository.findByUserIdOrderByIdAsc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(item));
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getUserItems(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRepository).findByUserIdOrderByIdAsc(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetUserItemsNoItems() {
        when(itemRepository.findByUserIdOrderByIdAsc(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getUserItems(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserItemsWithInvalidPagination() {
        assertThrows(IllegalArgumentException.class, () -> itemService.getUserItems(1L, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> itemService.getUserItems(1L, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> itemService.getUserItems(1L, null, 10));
        assertThrows(IllegalArgumentException.class, () -> itemService.getUserItems(1L, 0, null));
    }

    // ==================== ТЕСТЫ ADD COMMENT ====================

    @Test
    void testAddCommentValidData() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        when(bookingRepository.existsByBookerIdAndItemIdAndStatus(
                eq(2L), eq(1L), eq(BookingStatus.APPROVED)))
                .thenReturn(true);

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
    }

    @Test
    void testAddCommentUserNotFound_() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(99L, 1L, commentDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testAddCommentItemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(2L, 99L, commentDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }


    // ==================== ТЕСТЫ ПРИВАТНЫХ МЕТОДОВ (через публичные) ====================

    @Test
    void testCreatePageableWithValidParams() {
        // Тестируем через searchItems, который использует createPageable
        itemService.searchItems("drill", 10, 5);
        verify(itemRepository).search(eq("drill"), any(Pageable.class));
    }

    @Test
    void testCreatePageableWithZeroFrom() {
        itemService.searchItems("drill", 0, 10);
        verify(itemRepository).search(eq("drill"), any(Pageable.class));
    }

    @Test
    void testCreatePageableWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> itemService.searchItems("drill", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> itemService.searchItems("drill", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> itemService.searchItems("drill", null, 10));
        assertThrows(IllegalArgumentException.class, () -> itemService.searchItems("drill", 0, null));
    }
}