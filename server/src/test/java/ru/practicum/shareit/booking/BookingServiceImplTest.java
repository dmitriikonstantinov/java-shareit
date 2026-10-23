package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
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
class BookingServiceImplTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

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

    private Booking booking(Long id, Item item, User booker, BookingStatus status,
                            LocalDateTime start, LocalDateTime end) {
        Booking b = new Booking();
        b.setId(id);
        b.setItem(item);
        b.setBooker(booker);
        b.setStatus(status);
        b.setStart(start);
        b.setEnd(end);
        return b;
    }

    private BookingCreateDto createDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    @Test
    void create_success() {
        User booker = user(1L);
        Item item = item(10L, user(2L), true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBooking(eq(10L), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingDto result = bookingService.create(1L, createDto(10L, start, end));

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(1L, createDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_itemNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(1L, createDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_itemNotAvailable_throwsValidation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, user(2L), false)));

        assertThatThrownBy(() -> bookingService.create(1L, createDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_ownerBooksOwnItem_throwsNotFound() {
        User owner = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, owner, true)));

        assertThatThrownBy(() -> bookingService.create(1L, createDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_overlapping_throwsConflict() {
        User booker = user(1L);
        Item item = item(10L, user(2L), true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBooking(eq(10L), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(1L, createDto(10L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_true_success() {
        User owner = user(2L);
        Item item = item(10L, owner, true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.approve(2L, 100L, true);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void approve_false_success() {
        User owner = user(2L);
        Item item = item(10L, owner, true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.approve(2L, 100L, false);

        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void approve_bookingNotFound_throws() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approve(2L, 100L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_notOwner_throwsForbidden() {
        Item item = item(10L, user(2L), true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.approve(999L, 100L, true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyProcessed_throwsValidation() {
        Item item = item(10L, user(2L), true);
        Booking b = booking(100L, item, user(1L), BookingStatus.APPROVED,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.approve(2L, 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_asBooker_success() {
        Item item = item(10L, user(2L), true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThat(bookingService.getById(1L, 100L).getId()).isEqualTo(100L);
    }

    @Test
    void getById_asOwner_success() {
        Item item = item(10L, user(2L), true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThat(bookingService.getById(2L, 100L).getId()).isEqualTo(100L);
    }

    @Test
    void getById_notParticipant_throwsNotFound() {
        Item item = item(10L, user(2L), true);
        Booking b = booking(100L, item, user(1L), BookingStatus.WAITING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.getById(999L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_bookingNotFound_throws() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(1L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookings_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getUserBookings(1L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookings_all_returnsAll() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.ALL)).hasSize(1);
    }

    @Test
    void getUserBookings_current_filter() {
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        now.minusHours(1), now.plusHours(1)),          // current
                booking(2L, item(11L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        now.plusDays(1), now.plusDays(2))              // future
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.CURRENT)).hasSize(1);
    }

    @Test
    void getUserBookings_past_filter() {
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        now.minusDays(2), now.minusDays(1))
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.PAST)).hasSize(1);
    }

    @Test
    void getUserBookings_future_filter() {
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        now.plusDays(1), now.plusDays(2))
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.FUTURE)).hasSize(1);
    }

    @Test
    void getUserBookings_waiting_filter() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.WAITING,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
                booking(2L, item(11L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.WAITING)).hasSize(1);
    }

    @Test
    void getUserBookings_rejected_filter() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.findByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.REJECTED,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        ));

        assertThat(bookingService.getUserBookings(1L, BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void getOwnerBookings_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOwnerBookings(1L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnerBookings_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(2L)).thenReturn(List.of(
                booking(1L, item(10L, user(2L), true), user(1L), BookingStatus.APPROVED,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))
        ));

        assertThat(bookingService.getOwnerBookings(2L, BookingState.ALL)).hasSize(1);
    }
}