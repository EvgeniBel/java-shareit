package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Все бронирования пользователя с пагинацией
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    // Все бронирования вещей владельца с пагинацией
    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.userId = :ownerId) " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Проверка пересекающихся бронирований
    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :endDate " +
            "AND b.end > :startDate")
    List<Booking> findOverlappingApprovedBookings(@Param("itemId") Long itemId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Для комментариев - бронировал ли пользователь вещь
    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                           Long itemId,
                                                           BookingStatus status,
                                                           LocalDateTime end);

    // Все бронирования конкретной вещи
    List<Booking> findAllByItemIdOrderByStartAsc(Long itemId);
}
