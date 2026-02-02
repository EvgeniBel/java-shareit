package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ItemRequest save(ItemRequest itemRequest) {
        if (itemRequest.getId() == null) {
            itemRequest.setId(idGenerator.getAndIncrement());
        }
        requests.put(itemRequest.getId(), itemRequest);
        return itemRequest;
    }

    public Optional<ItemRequest> findById(Long requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    public List<ItemRequest> findByRequestorId(Long requestorId) {
        return requests.values().stream()
                .filter(request -> request.getRequestorId().equals(requestorId))
                .sorted((r1, r2) -> r2.getCreated().compareTo(r1.getCreated())) // Новые первыми
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAllExceptUser(Long userId) {
        return requests.values().stream()
                .filter(request -> !request.getRequestorId().equals(userId))
                .sorted((r1, r2) -> r2.getCreated().compareTo(r1.getCreated())) // Новые первыми
                .collect(Collectors.toList());
    }

    public void deleteById(Long requestId) {
        requests.remove(requestId);
    }
}
