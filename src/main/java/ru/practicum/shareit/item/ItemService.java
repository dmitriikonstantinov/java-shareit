package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemCreateDto itemDto, Long userId);

    ItemDto getById(Long id);

    List<ItemDto> getAll(Long userId);

    ItemDto update(Long id, ItemCreateDto itemDto, Long userId);

    void delete(Long id);

    List<ItemDto> search(String text);
}
