package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING,      // ожидает подтверждения
    APPROVED,     // подтверждено владельцем
    REJECTED,     // отклонено владельцем
    CANCELED      // отменено создателем
}