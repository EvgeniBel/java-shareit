package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    private final LocalDateTime now = LocalDateTime.now();
    @Autowired
    private JacksonTester<BookingDto> json;
    @Autowired
    private JacksonTester<BookingResponseDto> responseJson;

    @Test
    void testSerializeBookingDto() throws IOException {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(now)
                .end(now.plusDays(1))
                .itemId(10L)
                .bookerId(20L)
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathValue("$.start");
        assertThat(result).hasJsonPathValue("$.end");
        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).hasJsonPathNumberValue("$.bookerId");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
    }

    @Test
    void testDeserializeBookingDto() throws IOException {
        String jsonContent = String.format(
                "{\"id\":1,\"start\":\"%s\",\"end\":\"%s\",\"itemId\":10,\"bookerId\":20}",
                now.toString(), now.plusDays(1).toString());

        BookingDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getStart()).isEqualTo(now);
        assertThat(dto.getEnd()).isEqualTo(now.plusDays(1));
        assertThat(dto.getItemId()).isEqualTo(10);
        assertThat(dto.getBookerId()).isEqualTo(20);
    }

    @Test
    void testSerializeBookingResponseDto() throws IOException {
        UserDto user = UserDto.builder().id(20L).name("User").email("user@test.com").build();
        ItemDto item = ItemDto.builder().id(10L).name("Item").build();

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(now)
                .end(now.plusDays(1))
                .booker(user)
                .item(item)
                .build();

        JsonContent<BookingResponseDto> result = responseJson.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathValue("$.start");
        assertThat(result).hasJsonPathValue("$.end");
        assertThat(result).hasJsonPathNumberValue("$.booker.id");
        assertThat(result).hasJsonPathNumberValue("$.item.id");
    }
}