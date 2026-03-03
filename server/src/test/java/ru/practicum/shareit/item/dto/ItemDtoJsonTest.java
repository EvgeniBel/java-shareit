package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testSerializeItemDto() throws IOException {
        BookingShortDto lastBooking = new BookingShortDto(5L, 10L);
        BookingShortDto nextBooking = new BookingShortDto(6L, 20L);
        CommentDto comment = new CommentDto();
        comment.setId(7L);
        comment.setText("Great item");
        comment.setAuthorName("User");

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .owner(2L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(3L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathNumberValue("$.owner", 2);
        assertThat(result).hasJsonPathStringValue("$.name", "Drill");
        assertThat(result).hasJsonPathStringValue("$.description", "Electric drill");
        assertThat(result).hasJsonPathBooleanValue("$.available", true);
        assertThat(result).hasJsonPathNumberValue("$.requestId", 3);
        assertThat(result).hasJsonPathNumberValue("$.lastBooking.id", 5);
        assertThat(result).hasJsonPathNumberValue("$.nextBooking.id", 6);
        assertThat(result).hasJsonPathNumberValue("$.comments[0].id", 7);
    }

    @Test
    void testDeserializeItemDto() throws IOException {
        String jsonContent = "{\"id\":1,\"owner\":2,\"name\":\"Drill\"," +
                "\"description\":\"Electric drill\",\"available\":true,\"requestId\":3}";

        ItemDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getOwner()).isEqualTo(2);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Electric drill");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(3);
    }

    @Test
    void testSerializeItemDtoWithNullFields() throws IOException {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.name", "Drill");
        assertThat(result).hasJsonPath("$.owner");
        assertThat(result).extractingJsonPathNumberValue("$.owner").isNull();  // owner = null
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isNull();  // description = null
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isNull();  // available = null
    }
}
