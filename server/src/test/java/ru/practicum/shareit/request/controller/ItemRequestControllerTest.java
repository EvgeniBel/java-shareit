package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestControllerTest {
    static final String USER_ID_HEADER = "X-Sharer-User-Id";
    final LocalDateTime now = LocalDateTime.now();
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    ItemRequestService itemRequestService;
    ItemRequestDto itemRequestDto;
    ItemRequestDto itemRequestDtoWithoutCreated;
    ItemRequestWithItemsDto itemRequestWithItemsDto;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(now)
                .build();

        itemRequestDtoWithoutCreated = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(null)
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(1L)
                .build();

        itemRequestWithItemsDto = ItemRequestWithItemsDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(now)
                .items(List.of(itemDto))
                .build();
    }

    @Test
    void testCreateItemRequest() throws Exception {
        when(itemRequestService.createItemRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void testGetUserItemRequests() throws Exception {
        when(itemRequestService.getUserItemRequests(1L))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].id").value(1L));
    }

    @Test
    void testGetAllItemRequests() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetAllItemRequestsWithCustomParams() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(1), eq(5)))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemRequest() throws Exception {
        when(itemRequestService.getItemRequest(1L, 1L))
                .thenReturn(itemRequestWithItemsDto);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items[0].id").value(1L));
    }

    @Test
    void testCreateItemRequestWhenCreatedIsNull() throws Exception {
        when(itemRequestService.createItemRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDtoWithoutCreated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.created").exists());

        verify(itemRequestService).createItemRequest(eq(1L), argThat(dto -> dto.getCreated() != null));
    }

    @Test
    void testGetUserItemRequestsWhenEmptyList() throws Exception {
        when(itemRequestService.getUserItemRequests(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllItemRequestsWithDefaultParams() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(eq(1L), eq(0), eq(10));
    }

    @Test
    void testGetAllItemRequestsWithZeroFrom() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(5)))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(eq(1L), eq(0), eq(5));
    }

    @Test
    void testGetAllItemRequestsWithLargeFrom() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(100), eq(10)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "100")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(eq(1L), eq(100), eq(10));
    }

    @Test
    void testGetAllItemRequestsWithMinSize() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(1)))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllItemRequests(eq(1L), eq(0), eq(1));
    }

    @Test
    void testGetAllItemRequestsWhenServiceReturnsEmptyList() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetItemRequestWhenRequestNotFound() throws Exception {
        when(itemRequestService.getItemRequest(1L, 99L))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/99")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateItemRequestWithInvalidData() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .description("")
                .build();

        when(itemRequestService.createItemRequest(eq(1L), any(ItemRequestDto.class)))
                .thenThrow(new ValidationException("Description cannot be empty"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserItemRequestsWithInvalidUserId() throws Exception {
        when(itemRequestService.getUserItemRequests(999L))
                .thenThrow(new NotFoundException("User не найден"));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateItemRequestWithoutUserIdHeader() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }

    @Test
    void testGetItemRequestWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isInternalServerError())  // Ожидаем 500
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }

    @Test
    void testCreateItemRequestWithNullDescription() throws Exception {
        ItemRequestDto nullDescriptionDto = ItemRequestDto.builder()
                .description(null)
                .requestorId(1L)
                .build();

        when(itemRequestService.createItemRequest(eq(1L), any(ItemRequestDto.class)))
                .thenThrow(new ValidationException("Описание не должно быть null"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDescriptionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllItemRequestsWithNegativeFrom() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(-1), eq(10)))
                .thenThrow(new IllegalArgumentException("From не может быть отрицательным"));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())  // 500 вместо 400
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }

    @Test
    void testGetAllItemRequestsWithZeroSize() throws Exception {
        when(itemRequestService.getAllItemRequests(eq(1L), eq(0), eq(0)))
                .thenThrow(new IllegalArgumentException("Size должен быть положительным"));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError())  // 500 вместо 400
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }
}