package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingMapperTest {

    final LocalDateTime now = LocalDateTime.now();
    BookingMapper bookingMapper;
    Booking booking;
    ItemDto itemDto;
    UserDto userDto;
    BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper();

        booking = Booking.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .bookerId(2L)
                .status(BookingStatus.WAITING)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Jakson")
                .description("Electric drill")
                .available(true)
                .build();

        userDto = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    void testMapToBooking() {
        Booking result = bookingMapper.mapToBooking(bookingRequestDto, 2L);

        assertNotNull(result);
        assertEquals(bookingRequestDto.getStart(), result.getStart());
        assertEquals(bookingRequestDto.getEnd(), result.getEnd());
        assertEquals(bookingRequestDto.getItemId(), result.getItemId());
        assertEquals(2L, result.getBookerId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertNull(result.getId());
    }

    @Test
    void testMapToResponseDto() {
        BookingResponseDto result = bookingMapper.mapToResponseDto(booking, itemDto, userDto);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus(), result.getStatus());
        assertEquals(itemDto, result.getItem());
        assertEquals(userDto, result.getBooker());
    }

    @Test
    void testMapToShortDtoWithValidBooking() {
        BookingShortDto result = bookingMapper.mapToShortDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getBookerId(), result.getBookerId());
    }

    @Test
    void testMapToShortDtoWithNull() {
        BookingShortDto result = bookingMapper.mapToShortDto(null);
        assertNull(result);
    }

    @Test
    void testMapToResponseDtoListWithEmptyList() {
        List<Booking> emptyList = List.of();
        Function<Long, ItemDto> itemProvider = id -> itemDto;
        Function<Long, UserDto> userProvider = id -> userDto;

        List<BookingResponseDto> result = bookingMapper.mapToResponseDtoList(emptyList, itemProvider, userProvider);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMapToResponseDtoListWithBookings() {
        List<Booking> bookings = List.of(booking);
        Function<Long, ItemDto> itemProvider = id -> itemDto;
        Function<Long, UserDto> userProvider = id -> userDto;

        List<BookingResponseDto> result = bookingMapper.mapToResponseDtoList(bookings, itemProvider, userProvider);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }
}