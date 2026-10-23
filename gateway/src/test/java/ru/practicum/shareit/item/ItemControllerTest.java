package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void testCreateItem() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Дрель");
        createDto.setDescription("Мощная");
        createDto.setAvailable(true);

        ItemDto response = new ItemDto();
        response.setId(1L);
        response.setName("Дрель");

        when(itemClient.createItem(anyLong(), any())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllItems() throws Exception {
        when(itemClient.getAllItems(anyLong())).thenReturn(ResponseEntity.ok(java.util.List.of()));
        mockMvc.perform(get("/items").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testGetItem() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(1L);
        when(itemClient.getItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testUpdateItem() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("Новое имя");
        when(itemClient.updateItem(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok(new ItemDto()));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteItem() throws Exception {
        when(itemClient.deleteItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(delete("/items/1").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testSearchItems() throws Exception {
        when(itemClient.searchItems(anyLong(), any())).thenReturn(ResponseEntity.ok(java.util.List.of()));
        mockMvc.perform(get("/items/search?text=дрель").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testAddComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель");
        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok(commentDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }
}