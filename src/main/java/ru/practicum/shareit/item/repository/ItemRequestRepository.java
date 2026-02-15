package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Запросы других пользователей (с пагинацией)
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.requestorId != :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllExceptUser(@Param("userId") Long userId, Pageable pageable);

    // Запросы других пользователей (без пагинации)
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.requestorId != :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllExceptUser(@Param("userId") Long userId);

    // Подсчет количества запросов пользователя
    long countByRequestorId(Long requestorId);
}