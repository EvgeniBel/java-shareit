package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnauthorizedAccessException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем с ID={}", userId);

        userService.getUserById(userId);

        Item item = itemMapper.mapToItem(itemDto);
        item.setUserId(userId);
        item.setId(null);

        validateItemFields(item);

        // Проверка requestId если он указан
        if (item.getRequestId() != null) {
            itemRequestRepository.findById(item.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Запрос с ID=%d не найден", item.getRequestId())));
        }

        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с ID={}", savedItem.getId());

        return itemMapper.mapToDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        log.info("Получение вещи с ID={} пользователем ID={}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Вещь с ID=%d не найдена", itemId)));

        ItemDto itemDto = itemMapper.mapToDto(item);

        // Добавляем комментарии
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentDtos);

        // Если пользователь - владелец, добавляем даты бронирований
        if (item.getUserId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            // Последнее бронирование (прошедшее)
            Optional<Booking> lastBooking = bookingRepository
                    .findFirstByItemIdAndEndBeforeOrderByEndDesc(item.getId(), now);
            lastBooking.ifPresent(booking ->
                    itemDto.setLastBooking(bookingMapper.mapToShortDto(booking)));

            // Следующее бронирование (будущее)
            Optional<Booking> nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), now);
            nextBooking.ifPresent(booking ->
                    itemDto.setNextBooking(bookingMapper.mapToShortDto(booking)));
        }

        return itemDto;
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        userService.getUserById(userId);
        Item existingItem = getItemByIdAndCheckOwner(itemId, userId);
        itemRepository.delete(existingItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи с ID={} пользователем ID={}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Вещь с ID=%d не найдена", itemId)));

        if (!item.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    String.format("Пользователь с ID=%d не является владельцем вещи", userId));
        }

        // Обновляем только переданные поля
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.info("Вещь с ID={} обновлена", itemId);

        return itemMapper.mapToDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        log.info("Поиск вещей по запросу: '{}'", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        Pageable pageable = createPageable(from, size);

        return itemRepository.search(text, pageable).stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByRequestId(Long requestId) {
        if (requestId != null) {
            itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        return itemRepository.findByRequestId(requestId);
    }

    private Item getItemByIdAndCheckOwner(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Вещь с ID=%d не найдена", itemId)));
        if (!item.getUserId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Вещь с ID=%d не найдена у пользователя с ID=%d", itemId, userId)
            );
        }
        return item;
    }

    @Override
    public List<ItemDto> getUserItems(Long userId, Integer from, Integer size) {
        log.info("Получение вещей пользователя с ID={}", userId);

        Pageable pageable = createPageable(from, size);

        return itemRepository.findByUserIdOrderByIdAsc(userId, pageable).stream()
                .map(itemMapper::mapToDto)
                .collect(Collectors.toList());
    }

    private Pageable createPageable(Integer from, Integer size) {
        if (from == null || size == null || from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }
        int page = from / size;
        return PageRequest.of(page, size);
    }

    private void validateItemFields(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Имя вещи не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание вещи не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца с ID={} с датами бронирований", ownerId);

        // Проверяем существование пользователя
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByUserId(ownerId);
        List<ItemDto> itemDtos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Item item : items) {
            ItemDto itemDto = itemMapper.mapToDto(item);

            // Последнее бронирование (прошедшее)
            Optional<Booking> lastBooking = bookingRepository
                    .findFirstByItemIdAndEndBeforeOrderByEndDesc(item.getId(), now);
            lastBooking.ifPresent(booking ->
                    itemDto.setLastBooking(bookingMapper.mapToShortDto(booking)));

            // Следующее бронирование (будущее)
            Optional<Booking> nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), now);
            nextBooking.ifPresent(booking ->
                    itemDto.setNextBooking(bookingMapper.mapToShortDto(booking)));

            // Добавляем комментарии
            List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
            List<CommentDto> commentDtos = comments.stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());
            itemDto.setComments(commentDtos);

            itemDtos.add(itemDto);
        }

        return itemDtos;
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Добавление комментария к вещи ID={} пользователем ID={}", itemId, userId);

        // Проверяем существование пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверяем существование вещи
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверяем, что пользователь действительно брал вещь в аренду
        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }

        // Создаем комментарий
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий добавлен с ID={}", savedComment.getId());

        return CommentMapper.toCommentDto(savedComment);
    }
}