package ru.practicum.shareit.item;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Поиск вещей по владельцу с пагинацией
    List<Item> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);

    // Поиск вещей по владельцу без пагинации
    List<Item> findByUserId(Long userId);

    // Поиск по тексту в названии и описании (только доступные вещи)
    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true " +
            "ORDER BY i.id ASC")
    List<Item> search(@Param("text") String text, Pageable pageable);

    // Поиск по ID запроса (для вещей, созданных по запросу)
    List<Item> findByRequestId(Long requestId);

    // Поиск всех вещей с непустым requestId
    @Query("SELECT i FROM Item i WHERE i.requestId IS NOT NULL")
    List<Item> findItemsWithRequests();

    // Поиск по нескольким ID запросов
    @Query("SELECT i FROM Item i WHERE i.requestId IN :requestIds")
    List<Item> findByRequestIdIn(@Param("requestIds") List<Long> requestIds);

    // Проверка существования вещи у пользователя
    boolean existsByIdAndUserId(Long id, Long userId);

    // Удаление вещи по ID пользователя и ID вещи
    void deleteByIdAndUserId(Long id, Long userId);

    // Подсчет количества вещей у пользователя
    long countByUserId(Long userId);
}
