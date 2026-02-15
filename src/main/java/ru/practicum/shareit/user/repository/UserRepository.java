package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск пользователя по email (точное совпадение)
    Optional<User> findByEmail(String email);

    // Проверка существования email
    boolean existsByEmail(String email);

    // Проверка существования email у другого пользователя
    @Query("SELECT COUNT(u) > 0 FROM User u " +
            "WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email,
                                  @Param("userId") Long userId);

    // Поиск пользователей по части email (для админки)
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContaining(@Param("email") String email);

    // Поиск пользователей по части имени
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContaining(@Param("name") String name);
}
