package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(BookingClient.class)
class BookingClientTest {

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void testBookItem() {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)
        );

        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.bookItem(1L, dto);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetBookings() {
        server.expect(requestTo("http://localhost:9090/bookings?state=ALL&from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = bookingClient.getBookings(1L, BookingState.ALL, 0, 10);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetBooking() {
        server.expect(requestTo("http://localhost:9090/bookings/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.getBooking(1L, 1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testApproveBooking() {
        server.expect(requestTo("http://localhost:9090/bookings/1?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var response = bookingClient.approveBooking(1L, 1L, true);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetOwnerBookings() {
        server.expect(requestTo("http://localhost:9090/bookings/owner?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = bookingClient.getOwnerBookings(1L, BookingState.ALL);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testGetBookingNotFound() {
        server.expect(requestTo("http://localhost:9090/bookings/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not found"));

        var response = bookingClient.getBooking(1L, 999L);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testGetBookingServerError() {
        server.expect(requestTo("http://localhost:9090/bookings/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error"));

        var response = bookingClient.getBooking(1L, 1L);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}