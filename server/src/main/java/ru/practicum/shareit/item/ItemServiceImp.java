package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImp implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(ItemCreateDto itemDto, Long userid) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден!"));
            item.setRequest(request);
        }

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
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        Map<Long, List<Booking>> bookingsByItemId = bookingRepository
                .findAllByItemIdsAndApproved(itemIds).stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);

                    dto.setComments(
                            commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()).stream()
                                    .map(CommentMapper::toCommentDto)
                                    .collect(Collectors.toList())
                    );

                    List<Booking> itemBookings = bookingsByItemId
                            .getOrDefault(item.getId(), Collections.emptyList());

                    itemBookings.stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .max(Comparator.comparing(Booking::getEnd))
                            .ifPresent(b -> dto.setLastBooking(b.getEnd()));

                    itemBookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .ifPresent(b -> dto.setNextBooking(b.getStart()));

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
        System.out.println("=== addComment ===");
        System.out.println("Серверное время: " + LocalDateTime.now());
        System.out.println("userId: " + userId);
        System.out.println("itemId: " + itemId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        System.out.println("Серверное время: " + LocalDateTime.now());
        System.out.println("Бронь: " + bookingRepository.findByBookerIdOrderByStartDesc(userId).stream()
                .filter(b -> b.getItem().getId().equals(itemId))
                .findFirst().orElse(null));

        if (!bookingRepository.existsApprovedBooking(userId, itemId, BookingStatus.APPROVED,
                LocalDateTime.now())) {
            throw new ValidationException("Оставить отзыв можно только после завершения аренды");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentDto(saved);
    }

}
