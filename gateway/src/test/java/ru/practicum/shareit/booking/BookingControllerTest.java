package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private final String USER_ID_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingClient bookingClient;
    private BookingDto validBookingDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        validBookingDto = BookingDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    void testGetBookingsWithValidParams() throws Exception {
        when(bookingClient.getBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBookingsWithInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookingsWithNegativeFrom() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookingsWithZeroSize() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookingByIdWithValidId() throws Exception {
        when(bookingClient.getBookingById(1L, 1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBookingWithValidData() throws Exception {
        when(bookingClient.createBooking(eq(1L), any(BookingDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBookingWithNullStart() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .start(null)
                .end(now.plusDays(1))
                .itemId(1L)
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBookingWithPastStart() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .start(now.minusDays(1))
                .end(now.plusDays(1))
                .itemId(1L)
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBookingWithPastEnd() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .start(now.plusDays(1))
                .end(now.minusDays(1))
                .itemId(1L)
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBookingWithNullItemId() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(null)
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBookingStatusWithValidParams() throws Exception {
        when(bookingClient.updateBookingStatus(1L, 1L, true))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOwnerBookingsWithValidParams() throws Exception {
        when(bookingClient.getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void tetsCreateBookingWithoutUserIdHeader() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDto)))
                .andExpect(status().isInternalServerError());
    }
}