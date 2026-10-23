package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void testCreateBooking() throws Exception {
        BookItemRequestDto createDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Map<String, Object> response = Map.of("id", 1L, "status", "WAITING");
        when(bookingClient.bookItem(anyLong(), any())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBookings() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(), any(), any())).thenReturn(ResponseEntity.ok(java.util.List.of()));
        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testGetBooking() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(Map.of("id", 1L)));
        mockMvc.perform(get("/bookings/1").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testApproveBooking() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok(Map.of("status", "APPROVED")));
        mockMvc.perform(patch("/bookings/1?approved=true").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        when(bookingClient.getOwnerBookings(anyLong(), any())).thenReturn(ResponseEntity.ok(java.util.List.of()));
        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", "1")).andExpect(status().isOk());
    }
}