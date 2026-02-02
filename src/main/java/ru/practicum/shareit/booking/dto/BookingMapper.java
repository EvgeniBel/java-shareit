package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

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

    public Booking mapToBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .itemId(bookingDto.getItemId())
                .bookerId(bookingDto.getBookerId())
                .status(bookingDto.getStatus())
                .build();
    }

    public BookingDto mapToDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItemId())
                .bookerId(booking.getBookerId())
                .status(booking.getStatus())
                .build();
    }

    public BookingResponseDto mapToResponseDto(Booking booking, String itemName, String bookerName) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItemId())
                .itemName(itemName)
                .bookerId(booking.getBookerId())
                .bookerName(bookerName)
                .status(booking.getStatus())
                .build();
    }
}
