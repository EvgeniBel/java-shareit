package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
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
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Создание бронирования пользователем с ID={}", userId);

        if (bookingRequestDto == null) {
            throw new BookingValidationException("Данные бронирования не могут быть пустыми");
        }

        User booker = userService.getUserModelById(userId);
        Item item = getItemModelById(bookingRequestDto.getItemId());

        validateBookingCreation(userId, item, bookingRequestDto);

        Booking booking = bookingMapper.mapToBooking(bookingRequestDto, userId);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование с ID={} создано успешно", savedBooking.getId());

        ItemDto itemDto = itemMapper.mapToDto(item);
        UserDto userDto = userMapper.mapToDto(booker);

        return bookingMapper.mapToResponseDto(savedBooking, itemDto, userDto);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        Item item = getItemModelById(booking.getItemId());

        if (!item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не является владельцем вещи", userId));
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Статус бронирования ID={} изменен на {}", bookingId, updatedBooking.getStatus());

        User booker = userService.getUserModelById(updatedBooking.getBookerId());

        ItemDto itemDto = itemMapper.mapToDto(item);
        UserDto userDto = userMapper.mapToDto(booker);
        return bookingMapper.mapToResponseDto(updatedBooking, itemDto, userDto);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.info("Получение бронирования ID={} пользователем ID={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Бронирование с ID=%d не найдено", bookingId)));

        Item item = getItemModelById(booking.getItemId());

        if (!booking.getBookerId().equals(userId) && !item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не имеет доступа к бронированию", userId));
        }

        User booker = userService.getUserModelById(booking.getBookerId());
        ItemDto itemDto = itemMapper.mapToDto(item);
        UserDto userDto = userMapper.mapToDto(booker);
        return bookingMapper.mapToResponseDto(booking, itemDto, userDto);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований пользователя ID={} с состоянием {}", userId, state);

        userService.getUserModelById(userId);

        validatePaginationParams(from, size);

        Pageable pageable = createPageable(from, size);
        BookingFilterState bookingState = BookingFilterState.fromString(state);

        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageable);
        List<Booking> filteredBookings = filterBookingsByState(bookings, bookingState);

        return mapBookingsToResponseDto(filteredBookings);
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований владельца ID={} с состоянием {}", userId, state);

        userService.getUserModelById(userId);

        validatePaginationParams(from, size);

        Pageable pageable = createPageable(from, size);
        BookingFilterState bookingState = BookingFilterState.fromString(state);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerId(userId, pageable);
        List<Booking> filteredBookings = filterBookingsByState(bookings, bookingState);

        return mapBookingsToResponseDto(filteredBookings);
    }

    private Item getItemModelById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Вещь с ID=%d не найдена", itemId)));
    }

    private void validateBookingCreation(Long userId, Item item, BookingRequestDto bookingRequestDto) {
        if (item.getUserId().equals(userId)) {
            throw new BookingValidationException("Нельзя забронировать свою вещь");
        }

        if (item.getAvailable() == null || !item.getAvailable()) {
            throw new BookingValidationException("Вещь недоступна для бронирования");
        }

        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new BookingValidationException("Даты начала и окончания должны быть указаны");
        }

        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        if (start.isAfter(end)) {
            throw new BookingValidationException("Дата начала не может быть позже даты окончания");
        }

        if (start.isEqual(end)) {
            throw new BookingValidationException("Дата начала и окончания не могут совпадать");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Дата начала не может быть в прошлом");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingApprovedBookings(
                item.getId(), start, end);

        if (!overlappingBookings.isEmpty()) {
            throw new BookingValidationException("Вещь уже забронирована на указанные даты");
        }
    }

    private List<BookingResponseDto> mapBookingsToResponseDto(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    Item item = getItemModelById(booking.getItemId());  // ИСПРАВЛЕНО
                    User user = userService.getUserModelById(booking.getBookerId());

                    ItemDto itemDto = itemMapper.mapToDto(item);
                    UserDto userDto = userMapper.mapToDto(user);

                    return bookingMapper.mapToResponseDto(booking, itemDto, userDto);
                })
                .collect(Collectors.toList());
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, BookingFilterState state) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }

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
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                        .collect(Collectors.toList());
            case ALL:
            default:
                return bookings;
        }
    }

    private void validatePaginationParams(Integer from, Integer size) {
        if (from == null || size == null || from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }
    }

    private Pageable createPageable(Integer from, Integer size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}