package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // Все бронирования конкретной вещи
    List<Booking> findAllByItemIdOrderByStartAsc(Long itemId);

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +  // bookerId, не booker.id
            "AND b.itemId = :itemId " +        // itemId, не item.id
            "AND b.end < :now " +
            "AND b.status = 'APPROVED'")
    boolean hasUserBookedItem(@Param("bookerId") Long bookerId,
                              @Param("itemId") Long itemId,
                              @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +  // bookerId
            "AND b.itemId = :itemId " +        // itemId
            "AND b.end < :now " +
            "AND b.status = 'APPROVED'")
    boolean hasUserBookedAndApproved(@Param("bookerId") Long bookerId,
                                     @Param("itemId") Long itemId,
                                     @Param("now") LocalDateTime now);
}
