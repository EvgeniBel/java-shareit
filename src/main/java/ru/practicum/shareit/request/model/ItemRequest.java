package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "item_requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "description", nullable = false, length = 1000)
    String description;
    @Column(name = "requestor_id", nullable = false)
    Long requestorId;
    @Column(name = "created_date", nullable = false)
    LocalDateTime created;
}
