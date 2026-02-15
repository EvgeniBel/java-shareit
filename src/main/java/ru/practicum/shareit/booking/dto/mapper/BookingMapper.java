package ru.practicum.shareit.booking.dto.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public Booking mapToBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .itemId(bookingRequestDto.getItemId())
                .bookerId(bookerId)
                .status(BookingStatus.WAITING)
                .build();
    }

    public BookingResponseDto mapToResponseDto(Booking booking, ItemDto itemDto, UserDto bookerDto) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(bookerDto)
                .build();
    }

    public BookingShortDto mapToShortDto(Booking booking) {
        if (booking == null) return null;
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBookerId())
                .build();
    }

    public List<BookingResponseDto> mapToResponseDtoList(
            List<Booking> bookings,
            Function<Long, ItemDto> itemDtoProvider,
            Function<Long, UserDto> userDtoProvider) {

        if (bookings == null || bookings.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return bookings.stream()
                .map(booking -> {
                    ItemDto itemDto = itemDtoProvider.apply(booking.getItemId());
                    UserDto userDto = userDtoProvider.apply(booking.getBookerId());
                    return mapToResponseDto(booking, itemDto, userDto);
                })
                .collect(Collectors.toList());
    }
}
