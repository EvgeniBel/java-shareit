package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    private final LocalDateTime now = LocalDateTime.now();
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerializeCommentDto() throws IOException {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Great item!");
        dto.setAuthorName("User");
        dto.setCreated(now);

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.text", "Great item!");
        assertThat(result).hasJsonPathStringValue("$.authorName", "User");
        assertThat(result).hasJsonPathValue("$.created");
    }

    @Test
    void testDeserializeCommentDto() throws IOException {
        String jsonContent = String.format(
                "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"User\",\"created\":\"%s\"}",
                now.toString());

        CommentDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("User");
        assertThat(dto.getCreated()).isEqualTo(now);
    }
}
