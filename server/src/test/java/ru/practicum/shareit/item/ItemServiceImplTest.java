package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;

    @InjectMocks
    ItemServiceImp itemService;

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@m.ru");
        return u;
    }

    private Item item(Long id, User owner, boolean available) {
        Item i = new Item();
        i.setId(id);
        i.setName("Вещь");
        i.setDescription("Описание");
        i.setOwner(owner);
        i.setAvailable(available);
        return i;
    }

    private ItemCreateDto createDto(String name, String desc, Boolean available, Long requestId) {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }

    @Test
    void create_success_withoutRequest() {
        User owner = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });

        ItemDto result = itemService
                .create(createDto("Дрель", "Мощная", true, null), 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Дрель");
    }

    @Test
    void create_withRequest_success() {
        User owner = user(1L);
        ItemRequest req = new ItemRequest();
        req.setId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });

        ItemDto result = itemService
                .create(createDto("Дрель", "Мощная", true, 5L), 1L);

        assertThat(result.getRequestId()).isEqualTo(5L);
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService
                .create(createDto("Д", "О", true, null), 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_requestNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService
                .create(createDto("Д", "О", true, 5L), 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_success_withComments() {
        Item item = item(10L, user(1L), true);
        Comment c = new Comment();
        c.setId(1L);
        c.setText("Ок");
        c.setAuthor(user(2L));
        c.setCreated(LocalDateTime.now());
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(10L)).thenReturn(List.of(c));

        ItemDto result = itemService.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getComments()).hasSize(1);
    }

    @Test
    void getById_notFound_throws() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.getById(10L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_empty() {
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of());
        assertThat(itemService.getAll(1L)).isEmpty();
    }

    @Test
    void getAll_withBookingsAndComments() {
        User owner = user(1L);
        Item i1 = item(10L, owner, true);
        Item i2 = item(11L, owner, true);
        LocalDateTime now = LocalDateTime.now();

        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(i1, i2));
        when(commentRepository.findByItemIdIn(any())).thenReturn(List.of());
        Booking past = new Booking();
        past.setId(1L);
        past.setItem(i1);
        past.setBooker(user(2L));
        past.setStatus(BookingStatus.APPROVED);
        past.setStart(now.minusDays(2));
        past.setEnd(now.minusDays(1));
        Booking future = new Booking();
        future.setId(2L);
        future.setItem(i1);
        future.setBooker(user(3L));
        future.setStatus(BookingStatus.APPROVED);
        future.setStart(now.plusDays(1));
        future.setEnd(now.plusDays(2));
        when(bookingRepository.findAllByItemIdsAndApproved(any())).thenReturn(List.of(past, future));

        List<ItemDto> result = itemService.getAll(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLastBooking()).isEqualTo(past.getEnd());
        assertThat(result.get(0).getNextBooking()).isEqualTo(future.getStart());
    }

    @Test
    void update_success_allFields() {
        Item item = item(10L, user(1L), true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService
                .update(10L, createDto("Новое", "Новое описание", false, null), 1L);

        assertThat(result.getName()).isEqualTo("Новое");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_onlyName() {
        Item item = item(10L, user(1L), true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService
                .update(10L, createDto("Только имя", null, null, null), 1L);

        assertThat(result.getName()).isEqualTo("Только имя");
        assertThat(result.getDescription()).isEqualTo("Описание");
    }

    @Test
    void update_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService
                .update(10L, createDto("X", null, null, null), 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_itemNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService
                .update(10L, createDto("X", null, null, null), 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_notOwner_throwsForbidden() {
        Item item = item(10L, user(2L), true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> itemService
                .update(10L, createDto("X", null, null, null), 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void delete_callsRepository() {
        itemService.delete(10L);
        verify(itemRepository).deleteById(10L);
    }

    @Test
    void search_blank_returnsEmpty() {
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
        verifyNoInteractions(itemRepository);
    }

    @Test
    void search_found() {
        when(itemRepository.searchByText("дрель")).thenReturn(List.of(item(10L, user(1L), true)));
        assertThat(itemService.search("дрель")).hasSize(1);
    }

    @Test
    void search_nothingFound() {
        when(itemRepository.searchByText("xyz")).thenReturn(List.of());
        assertThat(itemService.search("xyz")).isEmpty();
    }

    @Test
    void addComment_success() {
        User author = user(1L);
        Item item = item(10L, user(2L), true);
        CommentDto dto = new CommentDto();
        dto.setText("Отлично");
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsApprovedBooking(eq(1L), eq(10L), eq(BookingStatus.APPROVED), any()))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CommentDto result = itemService.addComment(1L, 10L, dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Отлично");
    }

    @Test
    void addComment_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.addComment(1L, 10L, new CommentDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_itemNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.addComment(1L, 10L, new CommentDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_noApprovedBooking_throwsValidation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, user(2L), true)));
        when(bookingRepository.existsApprovedBooking(anyLong(), anyLong(), any(), any())).thenReturn(false);
        assertThatThrownBy(() -> itemService.addComment(1L, 10L, new CommentDto()))
                .isInstanceOf(ValidationException.class);
    }
}