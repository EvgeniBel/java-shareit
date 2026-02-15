package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

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
}
