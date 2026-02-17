package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String text;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(name = "created", nullable = false)
    LocalDateTime created;
}