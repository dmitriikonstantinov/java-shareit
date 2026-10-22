package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return requestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(this::mapWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size))
                .stream()
                .map(this::mapWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapWithItems(request);
    }

    private ItemRequestDto mapWithItems(ItemRequest request) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        List<ItemDto> items = itemRepository.findByRequestId(request.getId()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }
}