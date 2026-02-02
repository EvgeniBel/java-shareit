package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingFilterState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.WAITING;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Создание бронирования пользователем с ID={}", userId);

        userService.getUserById(userId);

        Item item = itemService.getItemById(bookingRequestDto.getItemId());

        validateBookingCreation(userId, item, bookingRequestDto);

        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, userId);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование с ID={} создано успешно", savedBooking.getId());

        User booker = userService.getUserById(booking.getBookerId());
        return bookingMapper.mapToResponseDto(savedBooking, item.getName(), booker.getName());
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        Item item = itemService.getItemById(booking.getItemId());

        if (!item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не является владельцем вещи", userId));
        }

        if (booking.getStatus() != WAITING) {
            throw new BookingValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Статус бронирования ID={} изменен на {}", bookingId, updatedBooking.getStatus());

        User booker = userService.getUserById(updatedBooking.getBookerId());
        return bookingMapper.mapToResponseDto(updatedBooking, item.getName(), booker.getName());
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.info("Получение бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        Item item = itemService.getItemById(booking.getItemId());

        if (!booking.getBookerId().equals(userId) && !item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не имеет доступа к бронированию", userId));
        }

        User booker = userService.getUserById(booking.getBookerId());
        return bookingMapper.mapToResponseDto(booking, item.getName(), booker.getName());
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований пользователя ID={} с состоянием {}", userId, state);

        userService.getUserById(userId);

        List<Booking> bookings = bookingRepository.findByBookerId(userId);

        BookingFilterState bookingState = BookingFilterState.fromString(state);

        List<Booking> filteredBookings = filterBookingsByState(bookings, bookingState);
        List<Booking> paginatedBookings = applyPagination(filteredBookings, from, size);

        return mapBookingsToResponseDto(paginatedBookings);
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, BookingFilterState state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                return bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case PAST:
                return bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case FUTURE:
                return bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case WAITING:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)  // Исправлено: BookingStatus
                        .collect(Collectors.toList());
            case REJECTED:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED) // Исправлено: BookingStatus
                        .collect(Collectors.toList());
            case CANCELED:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CANCELED) // Исправлено: BookingStatus
                        .collect(Collectors.toList());
            case ALL:
            default:
                return bookings;
        }
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {

        log.info("Получение бронирований владельца ID={} с состоянием {}", userId, state);

        userService.getUserById(userId);

        List<Booking> bookings = bookingRepository.findByItemOwnerId(userId);

        // Конвертируем строку в enum
        BookingFilterState bookingState = BookingFilterState.fromString(state);

        List<Booking> filteredBookings = filterBookingsByState(bookings, bookingState);
        List<Booking> paginatedBookings = applyPagination(filteredBookings, from, size);

        return mapBookingsToResponseDto(paginatedBookings);
    }


    private void validateBookingCreation(Long userId, Item item, BookingRequestDto bookingRequestDto) {

        if (item.getUserId().equals(userId)) {
            throw new BookingValidationException("Нельзя забронировать свою вещь");
        }

        if (item.getAvailable() == null || !item.getAvailable()) {
            throw new BookingValidationException("Вещь недоступна для бронирования");
        }

        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        if (start.isAfter(end)) {
            throw new BookingValidationException("Дата начала не может быть позже даты окончания");
        }

        if (start.isEqual(end)) {
            throw new BookingValidationException("Дата начала и окончания не могут совпадать");
        }

        List<Booking> existingBookings = bookingRepository.findByItemId(item.getId());

        boolean hasOverlap = existingBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .anyMatch(b -> isOverlapping(b.getStart(), b.getEnd(), start, end));

        if (hasOverlap) {
            throw new BookingValidationException("Вещь уже забронирована на указанные даты");
        }
    }

    private List<Booking> applyPagination(List<Booking> bookings, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        int start = Math.min(from, bookings.size());
        int end = Math.min(start + size, bookings.size());
        if (start >= bookings.size()) {
            return Collections.emptyList();
        }
        return bookings.subList(start, end);
    }

    private static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                         LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private List<BookingResponseDto> mapBookingsToResponseDto(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    User booker = userService.getUserById(booking.getBookerId());
                    return bookingMapper.mapToResponseDto(booking, item.getName(), booker.getName());
                })
                .collect(Collectors.toList());
    }
}