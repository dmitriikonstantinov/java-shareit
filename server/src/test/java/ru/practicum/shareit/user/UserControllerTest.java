package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService userService;

    private UserDto dto(Long id, String name, String email) {
        UserDto d = new UserDto();
        d.setId(id);
        d.setName(name);
        d.setEmail(email);
        return d;
    }

    @Test
    void create_success() throws Exception {
        when(userService.create(any())).thenReturn(dto(1L, "Дима", "dima@mail.ru"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto(null, "Дима", "dima@mail.ru"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дима"));
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(userService.create(any())).thenThrow(new ConflictException("email занят"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto(null, "Дима", "dima@mail.ru"))))
                .andExpect(status().isConflict());
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(userService.getAll()).thenReturn(List.of(dto(1L, "А", "a@m.ru"),
                dto(2L, "Б", "b@m.ru")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_success() throws Exception {
        when(userService.getById(1L)).thenReturn(dto(1L, "Дима", "dima@mail.ru"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("dima@mail.ru"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(99L)).thenThrow(new NotFoundException("нет"));

        mockMvc.perform(get("/users/99")).andExpect(status().isNotFound());
    }

    @Test
    void update_success() throws Exception {
        when(userService.update(anyLong(), any())).thenReturn(dto(1L, "Новое", "d@m.ru"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новое\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(userService.update(anyLong(), any())).thenThrow(new NotFoundException("нет"));

        mockMvc.perform(patch("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1")).andExpect(status().isOk());
        verify(userService).delete(1L);
    }
}