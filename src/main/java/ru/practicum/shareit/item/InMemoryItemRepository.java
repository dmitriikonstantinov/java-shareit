package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository{
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);

    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }

    @Override
    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lower = text.trim().toLowerCase();
        return items.values().stream().filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lower) ||
                        item.getDescription().toLowerCase().contains(lower)).collect(Collectors.toList());
    }
}
