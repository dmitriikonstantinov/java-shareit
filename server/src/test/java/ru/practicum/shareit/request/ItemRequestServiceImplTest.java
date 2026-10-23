package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    ItemRequestRepository requestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemRequestServiceImpl service;

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@m.ru");
        return u;
    }

    private ItemRequest request(Long id, User requestor) {
        ItemRequest r = new ItemRequest();
        r.setId(id);
        r.setDescription("Нужна дрель");
        r.setRequestor(requestor);
        r.setCreated(LocalDateTime.now());
        return r;
    }

    @Test
    void create_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        ItemRequestDto result = service.create(1L, dto);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(1L, new ItemRequestDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnRequests_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request(5L, user(1L))));
        when(itemRepository.findByRequestId(5L)).thenReturn(List.of());

        List<ItemRequestDto> result = service.getOwnRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
    }

    @Test
    void getOwnRequests_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOwnRequests(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllRequests_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(eq(1L), any()))
                .thenReturn(List.of(request(5L, user(2L))));
        when(itemRepository.findByRequestId(5L)).thenReturn(List.of());

        List<ItemRequestDto> result = service.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRequests_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getAllRequests(1L, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findById(5L)).thenReturn(Optional.of(request(5L, user(2L))));
        when(itemRepository.findByRequestId(5L)).thenReturn(List.of());

        ItemRequestDto result = service.getRequestById(1L, 5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getRequestById_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRequestById(1L, 5L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_requestNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRequestById(1L, 5L)).isInstanceOf(NotFoundException.class);
    }
}