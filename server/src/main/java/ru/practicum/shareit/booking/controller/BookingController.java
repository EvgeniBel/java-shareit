package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /bookings for user {} with state {}", userId, state);
        return bookingService.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        log.info("Server: GET /bookings/owner for user {} with state {}", userId, state);
        return bookingService.getOwnerBookings(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Server: GET /bookings/{} for user {}", bookingId, userId);
        return bookingService.getBooking(userId, bookingId);
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Server: POST /bookings for user {} with booking: {}", userId, bookingRequestDto);
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam Boolean approved) {
        log.info("Server: PATCH /bookings/{} for user {} with approved={}", bookingId, userId, approved);
        return bookingService.updateBookingStatus(userId, bookingId, approved);
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingResponseDto cancelBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long bookingId) {
        log.info("Server: PATCH /bookings/{}/cancel for user {}", bookingId, userId);
        return bookingService.cancelBooking(userId, bookingId);
    }
}
