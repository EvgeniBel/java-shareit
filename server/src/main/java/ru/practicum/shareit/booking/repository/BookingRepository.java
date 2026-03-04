package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

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

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM bookings b " +
            "WHERE b.booker_id = :bookerId " +
            "AND b.item_id = :itemId " +
            "AND b.end_date < :now " +
            "AND b.status = 'APPROVED'",
            nativeQuery = true)
    boolean hasUserBookedAndApproved(@Param("bookerId") Long bookerId,
                                     @Param("itemId") Long itemId,
                                     @Param("now") LocalDateTime now);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM bookings b " +
            "WHERE b.booker_id = :bookerId " +
            "AND b.item_id = :itemId " +
            "AND b.status = 'APPROVED'",
            nativeQuery = true)
    boolean existsByBookerIdAndItemIdAndStatus(@Param("bookerId") Long bookerId,
                                               @Param("itemId") Long itemId);

    @Query(value = "SELECT COUNT(*) > 0 FROM bookings b " +
            "WHERE b.booker_id = :bookerId " +
            "AND b.item_id = :itemId " +
            "AND b.end_date < :now " +
            "AND b.status = 'APPROVED'",
            nativeQuery = true)
    boolean hasCompletedBooking(@Param("bookerId") Long bookerId,
                                @Param("itemId") Long itemId,
                                @Param("now") LocalDateTime now);

    List<Booking> findByBookerIdAndItemIdAndStatus(Long bookerId, Long itemId, BookingStatus status);
}
