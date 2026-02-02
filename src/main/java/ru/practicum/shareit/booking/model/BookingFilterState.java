package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.BookingValidationException;

public enum BookingFilterState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    CANCELED;

    public static BookingFilterState fromString(String state) {
        try {
            return BookingFilterState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingValidationException("Неизвестный state: " + state);
        }
    }
}
