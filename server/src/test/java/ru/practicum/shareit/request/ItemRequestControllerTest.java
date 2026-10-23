package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService requestService;

    private ItemRequestDto dto(Long id) {
        ItemRequestDto d = new ItemRequestDto();
        d.setId(id);
        d.setDescription("Нужна дрель");
        return d;
    }

    @Test
    void create_success() throws Exception {
        when(requestService.create(anyLong(), any())).thenReturn(dto(1L));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getOwn_success() throws Exception {
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(dto(1L)));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_success() throws Exception {
        when(requestService.getAllRequests(eq(1L), anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_success() throws Exception {
        when(requestService.getRequestById(1L, 5L)).thenReturn(dto(5L));

        mockMvc.perform(get("/requests/5").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong())).thenThrow(new NotFoundException("нет"));

        mockMvc.perform(get("/requests/99").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }
}