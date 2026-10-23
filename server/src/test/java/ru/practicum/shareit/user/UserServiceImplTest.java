package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImp userService;

    private User user(Long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    @Test
    void create_success() {
        UserDto dto = new UserDto();
        dto.setName("Дима");
        dto.setEmail("dima@mail.ru");

        when(userRepository.existsByEmail("dima@mail.ru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto result = userService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дима");
        assertThat(result.getEmail()).isEqualTo("dima@mail.ru");
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        UserDto dto = new UserDto();
        dto.setName("Дима");
        dto.setEmail("dima@mail.ru");

        when(userRepository.existsByEmail("dima@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Дима", "d@m.ru")));

        UserDto result = userService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дима");
    }

    @Test
    void getById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(
                user(1L, "А", "a@m.ru"),
                user(2L, "Б", "b@m.ru")
        ));

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getAll_empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(userService.getAll()).isEmpty();
    }

    @Test
    void update_onlyName_success() {
        User existing = user(1L, "Старое", "old@m.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = new UserDto();
        patch.setName("Новое");

        UserDto result = userService.update(1L, patch);

        assertThat(result.getName()).isEqualTo("Новое");
        assertThat(result.getEmail()).isEqualTo("old@m.ru");
    }

    @Test
    void update_onlyEmail_success() {
        User existing = user(1L, "Имя", "old@m.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@m.ru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = new UserDto();
        patch.setEmail("new@m.ru");

        UserDto result = userService.update(1L, patch);

        assertThat(result.getEmail()).isEqualTo("new@m.ru");
        assertThat(result.getName()).isEqualTo("Имя");
    }

    @Test
    void update_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UserDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_emailTaken_throwsConflict() {
        User existing = user(1L, "Имя", "old@m.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@m.ru")).thenReturn(true);

        UserDto patch = new UserDto();
        patch.setEmail("taken@m.ru");

        assertThatThrownBy(() -> userService.update(1L, patch))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void delete_callsRepository() {
        userService.delete(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}