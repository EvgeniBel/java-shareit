package ru.practicum.shareit.item.dto.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemMapperTest {

    ItemMapper itemMapper;
    Item item;
    ItemDto itemDto;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapper();

        item = Item.builder()
                .id(1L)
                .userId(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(5L)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .owner(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(5L)
                .build();
    }

    @Test
    void testMapToItem() {
        Item result = itemMapper.mapToItem(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getOwner(), result.getUserId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertEquals(itemDto.getRequestId(), result.getRequestId());
    }

    @Test
    void testMapToDto() {
        ItemDto result = itemMapper.mapToDto(item);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getUserId(), result.getOwner());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(item.getRequestId(), result.getRequestId());
    }

    @Test
    void testMapToItemWithNullFields() {
        ItemDto dtoWithNulls = ItemDto.builder().build();
        Item result = itemMapper.mapToItem(dtoWithNulls);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getDescription());
    }

    @Test
    void testMapToDtoWithNullFields() {
        Item itemWithNulls = Item.builder().build();
        ItemDto result = itemMapper.mapToDto(itemWithNulls);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getName());
    }

    @Test
    void testMapToItemSetUserIdCorrectly() {
        ItemDto dto = ItemDto.builder().owner(10L).build();
        Item result = itemMapper.mapToItem(dto);
        assertEquals(10L, result.getUserId());
    }

    @Test
    void testMapToDtoSetOwnerCorrectly() {
        Item entity = Item.builder().userId(10L).build();
        ItemDto result = itemMapper.mapToDto(entity);
        assertEquals(10L, result.getOwner());
    }
}