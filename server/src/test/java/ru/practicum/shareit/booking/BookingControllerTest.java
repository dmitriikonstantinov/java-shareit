package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;

    private BookingDto dto(Long id, String status) {
        BookingDto d = new BookingDto();
        d.setId(id);
        d.setStatus(status);
        return d;
    }

    @Test
    void create_success() throws Exception {
        BookingCreateDto create = new BookingCreateDto();
        create.setItemId(10L);
        create.setStart(LocalDateTime.now().plusDays(1));
        create.setEnd(LocalDateTime.now().plusDays(2));
        when(bookingService.create(anyLong(), any())).thenReturn(dto(1L, "WAITING"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_success() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(dto(1L, "APPROVED"));

        mockMvc.perform(patch("/bookings/1").param("approved", "true")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_forbidden_returns403() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ForbiddenException("не владелец"));

        mockMvc.perform(patch("/bookings/1").param("approved", "true")
                        .header("X-Sharer-User-Id", "99"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_success() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenReturn(dto(1L, "WAITING"));

        mockMvc.perform(get("/bookings/1").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException("нет"));

        mockMvc.perform(get("/bookings/1").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_defaultState() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any())).thenReturn(List.of(dto(1L, "WAITING")));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOwnerBookings_success() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), any())).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}