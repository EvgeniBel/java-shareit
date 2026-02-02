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
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.BookingValidationException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // Проверяем существование пользователя
        userService.getUserById(userId);

        // Получаем вещь
        Item item = itemService.getItemById(bookingRequestDto.getItemId());

        // Проверки
        validateBookingCreation(userId, item, bookingRequestDto);

        // Создаем бронирование
        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, userId);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование с ID={} создано успешно", savedBooking.getId());

        // TODO: получить имена для ответа
        return bookingMapper.mapToResponseDto(savedBooking, item.getName(), "Имя пользователя");
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        // Получаем вещь
        Item item = itemService.getItemById(booking.getItemId());

        // Проверяем, что пользователь - владелец вещи
        if (!item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не является владельцем вещи", userId));
        }

        // Проверяем, что статус еще WAITING
        if (booking.getStatus() != StatusBooking.WAITING) {
            throw new BookingValidationException("Статус бронирования уже изменен");
        }

        // Обновляем статус
        booking.setStatus(approved ? StatusBooking.APPROVED : StatusBooking.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Статус бронирования ID={} изменен на {}", bookingId, updatedBooking.getStatus());

        // TODO: получить имена для ответа
        return bookingMapper.mapToResponseDto(updatedBooking, item.getName(), "Имя пользователя");
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.info("Получение бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        // Получаем вещь
        Item item = itemService.getItemById(booking.getItemId());

        // Проверяем права доступа
        if (!booking.getBookerId().equals(userId) && !item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не имеет доступа к бронированию", userId));
        }

        // TODO: получить имена для ответа
        return bookingMapper.mapToResponseDto(booking, item.getName(), "Имя пользователя");
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований пользователя ID={} с состоянием {}", userId, state);

        // Проверяем существование пользователя
        userService.getUserById(userId);

        List<Booking> bookings = bookingRepository.findByBookerId(userId);

        // Фильтруем по состоянию
        List<Booking> filteredBookings = filterBookingsByState(bookings, state);

        // Пагинация
        List<Booking> paginatedBookings = applyPagination(filteredBookings, from, size);

        // TODO: маппить с именами
        return paginatedBookings.stream()
                .map(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    return bookingMapper.mapToResponseDto(booking, item.getName(), "Имя пользователя");
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {

        log.info("Получение бронирований владельца ID={} с состоянием {}", userId, state);

        userService.getUserById(userId);

        List<Booking> bookings = bookingRepository.findByItemOwnerId(userId);

        List<Booking> filteredBookings = filterBookingsByState(bookings, state);

        List<Booking> paginatedBookings = applyPagination(filteredBookings, from, size);

        return paginatedBookings.stream()
                .map(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    return bookingMapper.mapToResponseDto(booking, item.getName(), "Имя пользователя");
                })
                .collect(Collectors.toList());
    }


    private void validateBookingCreation(Long userId, Item item, BookingRequestDto bookingRequestDto) {

        if (item.getUserId().equals(userId)) {
            throw new BookingNotFoundException("Нельзя забронировать свою вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
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
        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        boolean hasOverlap = existingBookings.stream()
                .filter(b -> b.getStatus() == StatusBooking.APPROVED)
                .anyMatch(b -> isOverlapping(b.getStart(), b.getEnd(), start, end));

        if (hasOverlap) {
            throw new BookingValidationException("Вещь уже забронирована на указанные даты");
        }
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "CURRENT":
                return bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case "PAST":
                return bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case "WAITING":
                return bookings.stream()
                        .filter(b -> b.getStatus() == StatusBooking.WAITING)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookings.stream()
                        .filter(b -> b.getStatus() == StatusBooking.REJECTED)
                        .collect(Collectors.toList());
            case "ALL":
            default:
                return bookings;
        }
    }

    private List<Booking> applyPagination(List<Booking> bookings, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }

        int start = Math.min(from, bookings.size());
        int end = Math.min(start + size, bookings.size());

        return bookings.subList(start, end);
    }
    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}