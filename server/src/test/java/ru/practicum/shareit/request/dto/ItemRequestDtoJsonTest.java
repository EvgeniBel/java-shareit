package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    private final LocalDateTime now = LocalDateTime.now();
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerializeItemRequestDto() throws IOException {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(now)
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.description", "Need a drill");
        assertThat(result).hasJsonPathValue("$.created");
    }

    @Test
    void testDeserializeItemRequestDto() throws IOException {
        String jsonContent = String.format(
                "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"%s\"}",
                now.toString());

        ItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(now);
    }

    @Test
    void testSerializeItemRequestDtoWithNullFields() throws IOException {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.description", "Need a drill");
        assertThat(result).hasJsonPath("$.id");  // Поле есть, но null
        assertThat(result).extractingJsonPathNumberValue("$.id").isNull();  // Проверяем, что оно null
        assertThat(result).hasJsonPath("$.created");  // Поле есть, но null
        assertThat(result).extractingJsonPathStringValue("$.created").isNull();  // Проверяем, что оно null
    }

    @Test
    void testDeserializeItemRequestDtoWithMissingFields() throws IOException {
        String jsonContent = "{\"description\":\"Need a drill\"}";

        ItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isNull();
    }

    @Test
    void testSerializeItemRequestDtoWithSpecialCharacters() throws IOException {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a drill & hammer!")
                .created(now)
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.description", "Need a drill & hammer!");
        assertThat(result).hasJsonPathValue("$.created");
    }
}