package ru.practicum.shareit.item.dto.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}