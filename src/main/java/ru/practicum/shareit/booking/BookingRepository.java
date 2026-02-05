package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final ItemRepository itemRepository;
    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public BookingRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(idGenerator.getAndIncrement());
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Optional<Booking> findById(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public List<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBookerId().equals(bookerId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findByItemOwnerId(Long ownerId) {
        List<Item> ownerItems = itemRepository.findByUserId(ownerId);
        Set<Long> ownerItemIds = ownerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        return bookings.values().stream()
                .filter(booking -> ownerItemIds.contains(booking.getItemId()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItemId().equals(itemId))
                .collect(Collectors.toList());
    }

    public void deleteById(Long bookingId) {
        bookings.remove(bookingId);
    }
}
