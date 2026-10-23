package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;

    private ItemDto dto(Long id, String name) {
        ItemDto d = new ItemDto();
        d.setId(id);
        d.setName(name);
        d.setAvailable(true);
        return d;
    }

    private ItemCreateDto create(String name) {
        ItemCreateDto c = new ItemCreateDto();
        c.setName(name);
        c.setDescription("D");
        c.setAvailable(true);
        return c;
    }

    @Test
    void create_success() throws Exception {
        when(itemService.create(any(), anyLong())).thenReturn(dto(1L, "Дрель"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(create("Дрель"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getAll_success() throws Exception {
        when(itemService.getAll(1L)).thenReturn(List.of(dto(1L, "A"), dto(2L, "B")));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_success() throws Exception {
        when(itemService.getById(1L)).thenReturn(dto(1L, "Дрель"));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(itemService.getById(99L)).thenThrow(new NotFoundException("нет"));

        mockMvc.perform(get("/items/99")).andExpect(status().isNotFound());
    }

    @Test
    void update_success() throws Exception {
        when(itemService.update(anyLong(), any(), anyLong())).thenReturn(dto(1L, "Новое"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новое\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое"));
    }

    @Test
    void update_forbidden_returns403() throws Exception {
        when(itemService.update(anyLong(), any(), anyLong())).thenThrow(new ForbiddenException("не владелец"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_success() throws Exception {
        mockMvc.perform(delete("/items/1")).andExpect(status().isOk());
    }

    @Test
    void search_success() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(dto(1L, "Дрель")));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment_success() throws Exception {
        CommentDto c = new CommentDto();
        c.setId(1L);
        c.setText("Ок");
        when(itemService.addComment(anyLong(), anyLong(), any())).thenReturn(c);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Ок\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Ок"));
    }
}