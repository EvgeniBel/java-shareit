package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.CommentDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemClient itemClient;
    private ItemDto validItemDto;
    private CommentDto validCommentDto;

    @BeforeEach
    void setUp() {
        validItemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        validCommentDto = new CommentDto();
        validCommentDto.setText("Great item!");
    }

    @Test
    void testGetItemsWithValidParams() throws Exception {
        when(itemClient.getItems(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemsWithNegativeFrom() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetItemsWithZeroSize() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetItemByIdWithValidId() throws Exception {
        when(itemClient.getItemById(1L, 1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateItemWithValidData() throws Exception {
        when(itemClient.createItem(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void tetsCreateItemWithNullName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name(null)
                .description("Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tetsCreateItemWithBlankName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("   ")
                .description("Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tetsCreateItemWithNullDescription() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Name")
                .description(null)
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tetsCreateItemWithNullAvailable() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Name")
                .description("Description")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tetsUpdateItemWithValidData() throws Exception {
        when(itemClient.updateItem(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteItemWithValidId() throws Exception {
        when(itemClient.deleteItem(1L, 1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void tetstSearchItemsWithValidText() throws Exception {
        when(itemClient.searchItems(eq("drill"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void tetsSearchItemsWithEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk());
    }

    @Test
    void testAddCommentWithValidData() throws Exception {
        when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testAddCommentWithNullText() throws Exception {
        CommentDto invalidDto = new CommentDto();
        invalidDto.setText(null);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetItemsByRequests() throws Exception {
        when(itemClient.getItemsByRequests(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }
}