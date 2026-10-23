package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Test
    void testCreateUser() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Дима");
        dto.setEmail("dima@mail.ru");
        when(userClient.createUser(any())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дима"));
    }

    @Test
    void testGetUser() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Дима");
        when(userClient.getUser(anyLong())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userClient.getAllUsers()).thenReturn(ResponseEntity.ok(java.util.List.of()));
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    void testUpdateUser() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("Новое имя");
        when(userClient.updateUser(anyLong(), any())).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUser() throws Exception {
        when(userClient.deleteUser(anyLong())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(delete("/users/1")).andExpect(status().isOk());
    }
}