package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplIntegrationTest {

    @Autowired
    BookingService bookingService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

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
    void testCreateBookingValidData() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto result = bookingService.createBooking(booker.getId(), requestDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void testCreateBookingWhenItemNotAvailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(booker.getId(), requestDto));
    }

    @Test
    void testCreateBookingWhenUserIsOwner() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(owner.getId(), requestDto));
    }

    @Test
    void testUpdateBookingStatusAsOwnerApprove() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(booker.getId(), requestDto);

        BookingResponseDto result = bookingService.updateBookingStatus(
                owner.getId(), createdBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void testUpdateBookingStatusAsOwnerReject() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(booker.getId(), requestDto);

        BookingResponseDto result = bookingService.updateBookingStatus(
                owner.getId(), createdBooking.getId(), false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void testGetBookingAsBooker() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(booker.getId(), requestDto);

        BookingResponseDto result = bookingService.getBooking(booker.getId(), createdBooking.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
    }

    @Test
    void testGetBookingAsOwner() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(booker.getId(), requestDto);

        BookingResponseDto result = bookingService.getBooking(owner.getId(), createdBooking.getId());

        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
    }

    @Test
    void testGetUserBookingsWithStateAll() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        bookingService.createBooking(booker.getId(), requestDto);

        var result = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void testCancelBookingAsBooker() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(item.getId())
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(booker.getId(), requestDto);

        BookingResponseDto result = bookingService.cancelBooking(booker.getId(), createdBooking.getId());

        assertEquals(BookingStatus.CANCELED, result.getStatus());
    }
}