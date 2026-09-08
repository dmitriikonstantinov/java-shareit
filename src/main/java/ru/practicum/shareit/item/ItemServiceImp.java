package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImp implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userid) {
        userRepository.findById(userid).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userid);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь не найдена!"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return itemRepository.findAll().stream().filter(item -> item.getOwner().equals(userId))
                .map(ItemMapper::toItemDto).toList();
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь не найдена!"));
        if (!item.getOwner().equals(userId)) {
            throw new RuntimeException("Редактировать может только владелец");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updateItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updateItem);
    }

    @Override
    public void delete(Long id) {
        itemRepository.deleteById(id);

    }

    @Override
    public List<ItemDto> search(String text) {
        List<Item> items = itemRepository.searchByText(text);

        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }


}
