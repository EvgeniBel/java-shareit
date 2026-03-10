package ru.practicum.shareit.item.dto.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentMapperTest {

    Comment comment;
    User author;
    Item item;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(2L)
                .name("Jackson")
                .email("Jackson@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    void tetstToCommentDto() {
        CommentDto result = CommentMapper.toCommentDto(comment);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getAuthor().getName(), result.getAuthorName());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    @Test
    void testToCommentDtoWithNull() {
        assertThrows(NullPointerException.class, () -> CommentMapper.toCommentDto(null));
    }

    @Test
    void testToCommentDto() {
        CommentDto result = CommentMapper.toCommentDto(comment);
        assertEquals("Jackson", result.getAuthorName());
    }

    @Test
    void testToCommentDtoPreserveCreatedTime() {
        CommentDto result = CommentMapper.toCommentDto(comment);
        assertEquals(comment.getCreated(), result.getCreated());
    }
}