package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImp implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemDto create(ItemCreateDto itemDto, Long userid) {
        User user = userRepository.findById(userid).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена!"));
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(
                commentRepository.findByItemId(id).stream()
                        .map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList())
        );
        return itemDto;
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findByOwnerId(userId).stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    dto.setComments(
                            commentRepository.findByItemId(item.getId()).stream()
                                    .map(CommentMapper::toCommentDto)
                                    .collect(Collectors.toList())
                    );

                    List<Booking> lastBookings = bookingRepository.findLastBooking(
                            item.getId(), now, PageRequest.of(0, 1));
                    if (!lastBookings.isEmpty()) {
                        dto.setLastBooking(lastBookings.get(0).getEnd());
                    }

                    List<Booking> nextBookings = bookingRepository.findNextBooking(
                            item.getId(), now, PageRequest.of(0, 1));
                    if (!nextBookings.isEmpty()) {
                        dto.setNextBooking(nextBookings.get(0).getStart());
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public ItemDto update(Long id, ItemCreateDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь не найдена!"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать может только владелец");
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
    @Transactional
    public void delete(Long id) {
        itemRepository.deleteById(id);

    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.searchByText(text);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!bookingRepository.existsApprovedBooking(userId, itemId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ValidationException("Оставить отзыв можно только после завершения аренды");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

}
