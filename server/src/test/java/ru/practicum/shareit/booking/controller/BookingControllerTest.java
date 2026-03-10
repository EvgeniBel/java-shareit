package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingControllerTest {
    static final String USER_ID_HEADER = "X-Sharer-User-Id";
    final LocalDateTime now = LocalDateTime.now();
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    BookingService bookingService;
    BookingResponseDto bookingResponseDto;
    BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    void testGetUserBookings() throws Exception {
        when(bookingService.getUserBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void testGetUserBookingsWithCustomParams() throws Exception {
        when(bookingService.getUserBookings(eq(1L), eq("WAITING"), eq(1), eq(5)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetBookingById() throws Exception {
        when(bookingService.getBooking(1L, 1L)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testCreateBooking() throws Exception {
        when(bookingService.createBooking(eq(1L), any(BookingRequestDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdateBookingStatus() throws Exception {
        when(bookingService.updateBookingStatus(eq(1L), eq(1L), eq(true)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testCancelBooking() throws Exception {
        when(bookingService.cancelBooking(1L, 1L)).thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/1/cancel")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}