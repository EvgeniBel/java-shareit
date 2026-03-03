package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    BookingMapper bookingMapper;
    @Mock
    UserService userService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    UserMapper userMapper;

    @InjectMocks
    BookingServiceImpl bookingService;

    User booker;
    User owner;
    Item item;
    Booking booking;
    BookingRequestDto bookingRequestDto;
    ItemDto itemDto;
    UserDto userDto;
    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .userId(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.WAITING)
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        userDto = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();
    }

    // ==================== ТЕСТЫ CREATE BOOKING ====================

    @Test
    void testCreateBookingValidData() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.mapToBooking(bookingRequestDto, 2L)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(booking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.createBooking(2L, bookingRequestDto);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(itemDto, result.getItem());
        assertEquals(userDto, result.getBooker());

        verify(bookingRepository).save(any(Booking.class));
        verify(bookingMapper).mapToResponseDto(booking, itemDto, userDto);
    }

    @Test
    void testCreateBookingWithNullRequest() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, null));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingWhenItemNotAvailable() {
        item.setAvailable(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingWhenUserIsOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(1L, bookingRequestDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBookingWithNullStart() {
        bookingRequestDto.setStart(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void testCreateBookingWithNullEnd() {
        bookingRequestDto.setEnd(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void testCreateBookingWithStartAfterEnd() {
        bookingRequestDto.setStart(now.plusDays(2));
        bookingRequestDto.setEnd(now.plusDays(1));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void testCreateBookingWithStartEqualsEnd() {
        bookingRequestDto.setStart(now.plusDays(1));
        bookingRequestDto.setEnd(now.plusDays(1));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void testCreateBookingWithStartInPast() {
        bookingRequestDto.setStart(now.minusDays(1));
        bookingRequestDto.setEnd(now.plusDays(1));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void testCreateBookingWithOverlappingDates() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findOverlappingApprovedBookings(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(2L, bookingRequestDto));
    }

    // ==================== ТЕСТЫ UPDATE BOOKING STATUS ====================

    @Test
    void testUpdateBookingStatusAsOwnerApprove() {
        Booking approvedBooking = Booking.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(approvedBooking.getId())
                .start(approvedBooking.getStart())
                .end(approvedBooking.getEnd())
                .status(BookingStatus.APPROVED)
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(approvedBooking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingStatusAsOwnerReject() {
        Booking rejectedBooking = Booking.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(rejectedBooking);
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(rejectedBooking.getId())
                .start(rejectedBooking.getStart())
                .end(rejectedBooking.getEnd())
                .status(BookingStatus.REJECTED)
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(rejectedBooking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingStatusBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.updateBookingStatus(1L, 99L, true));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingStatusItemNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.updateBookingStatus(1L, 1L, true));
    }

    @Test
    void testUpdateBookingStatusWhenNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(UnauthorizedAccessException.class, () ->
                bookingService.updateBookingStatus(3L, 1L, true));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingStatusWhenBookingNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.updateBookingStatus(1L, 1L, true));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== ТЕСТЫ GET BOOKING ====================

    @Test
    void testGetBookingAsBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(booking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.getBooking(2L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        verify(bookingMapper).mapToResponseDto(booking, itemDto, userDto);
    }

    @Test
    void testGetBookingAsOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(booking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.getBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void testGetBookingBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(2L, 99L));
    }

    @Test
    void testGetBookingItemNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(2L, 1L));
    }

    @Test
    void testGetBookingUserNotAuthorized() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(UnauthorizedAccessException.class, () ->
                bookingService.getBooking(3L, 1L));
    }

    // ==================== ТЕСТЫ CANCEL BOOKING ====================

    @Test
    void testCancelBookingAsBooker() {
        Booking canceledBooking = Booking.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.CANCELED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(canceledBooking);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(itemMapper.mapToDto(item)).thenReturn(itemDto);
        when(userMapper.mapToDto(booker)).thenReturn(userDto);

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(canceledBooking.getId())
                .start(canceledBooking.getStart())
                .end(canceledBooking.getEnd())
                .status(BookingStatus.CANCELED)
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDto(canceledBooking, itemDto, userDto))
                .thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.cancelBooking(2L, 1L);

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCancelBookingBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.cancelBooking(2L, 99L));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCancelBookingNotBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedAccessException.class, () ->
                bookingService.cancelBooking(3L, 1L));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCancelBookingBookingNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.cancelBooking(2L, 1L));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== ТЕСТЫ GET USER BOOKINGS ====================

    @Test
    void testGetUserBookingsStateALL() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(eq(2L), any());
        verify(bookingMapper).mapToResponseDtoList(anyList(), any(), any());
    }

    @Test
    void testGetUserBookingWithInvalidPagination() {
        when(userService.getUserModelById(2L)).thenReturn(booker);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(2L, "ALL", -1, 10));
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(2L, "ALL", 0, 0));
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(2L, "ALL", null, 10));
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(2L, "ALL", 0, null));
    }

    @Test
    void testGetUserBookingsWithInvalidState() {
        when(userService.getUserModelById(2L)).thenReturn(booker);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(2L, "INVALID_STATE", 0, 10));
    }

    @Test
    void testGetUserBookingsNoBookings() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(Collections.emptyList());

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== ТЕСТЫ GET OWNER BOOKINGS ====================

    @Test
    void testGetOwnerBookingsStateALL() {
        when(userService.getUserModelById(1L)).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerId(eq(1L), any()))
                .thenReturn(List.of(booking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByItemOwnerId(eq(1L), any());
        verify(bookingMapper).mapToResponseDtoList(anyList(), any(), any());
    }

    @Test
    void testGetOwnerBookingsWithInvalidPagination() {
        when(userService.getUserModelById(1L)).thenReturn(owner);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getOwnerBookings(1L, "ALL", -1, 10));
    }

    @Test
    void testGetOwnerBookingsWithInvalidState() {
        when(userService.getUserModelById(1L)).thenReturn(owner);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getOwnerBookings(1L, "INVALID_STATE", 0, 10));
    }

    // ==================== ТЕСТЫ ФИЛЬТРАЦИИ ПО STATE ====================

    @Test
    void testGetUserBookingsFilterByCurrent() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = Booking.builder()
                .id(2L)
                .start(now.minusHours(1))
                .end(now.plusHours(1))
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(currentBooking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(currentBooking.getId())
                .start(currentBooking.getStart())
                .end(currentBooking.getEnd())
                .status(currentBooking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "CURRENT", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserBookingsFilterByPast() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = Booking.builder()
                .id(2L)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(pastBooking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(pastBooking.getId())
                .start(pastBooking.getStart())
                .end(pastBooking.getEnd())
                .status(pastBooking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "PAST", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserBookingsFilterByFuture() {
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = Booking.builder()
                .id(2L)
                .start(now.plusDays(2))
                .end(now.plusDays(3))
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(futureBooking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(futureBooking.getId())
                .start(futureBooking.getStart())
                .end(futureBooking.getEnd())
                .status(futureBooking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "FUTURE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserBookingsFilterByWaiting() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "WAITING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
    }

    @Test
    void testGetUserBookingsFilterByRejected() {
        Booking rejectedBooking = Booking.builder()
                .id(2L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.REJECTED)
                .build();

        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(rejectedBooking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(rejectedBooking.getId())
                .start(rejectedBooking.getStart())
                .end(rejectedBooking.getEnd())
                .status(BookingStatus.REJECTED)
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "REJECTED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
    }

    // ==================== ТЕСТЫ ВСПОМОГАТЕЛЬНЫХ МЕТОДОВ ====================

    @Test
    void testGetItemDtoByIdWhenItemExists() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(List.of(booking));

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(booking.getId())
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(List.of(expectedResponse));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL", 0, 10);

        assertNotNull(result.get(0).getItem());
        assertEquals(itemDto, result.get(0).getItem());
    }

    @Test
    void testGetUserDtoByIdWhenUserExists() {
        // Только необходимые моки
        when(userService.getUserModelById(2L)).thenReturn(booker);

        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(Collections.emptyList());

        bookingService.getUserBookings(2L, "ALL", 0, 10);

        verify(userService, atLeast(1)).getUserModelById(2L);
    }

    @Test
    void testValidatePaginationParamsWithValidParams() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(Collections.emptyList());
        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Не должно выбросить исключение
        bookingService.getUserBookings(2L, "ALL", 0, 10);
        bookingService.getUserBookings(2L, "ALL", 10, 5);
    }

    @Test
    void testCreatePageableWithValidParams() {
        when(userService.getUserModelById(2L)).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(2L), any()))
                .thenReturn(Collections.emptyList());
        when(bookingMapper.mapToResponseDtoList(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        bookingService.getUserBookings(2L, "ALL", 0, 10);
        bookingService.getUserBookings(2L, "ALL", 10, 5);

        // Проверяем, что репозиторий вызван с Pageable
        verify(bookingRepository, times(2)).findAllByBookerIdOrderByStartDesc(eq(2L), any());
    }
}