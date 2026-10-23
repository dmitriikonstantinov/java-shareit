package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStateTest {

    @Test
    void testFromAll() {
        assertThat(BookingState.from("ALL")).contains(BookingState.ALL);
    }

    @Test
    void testFromCurrent() {
        assertThat(BookingState.from("CURRENT")).contains(BookingState.CURRENT);
    }

    @Test
    void testFromFuture() {
        assertThat(BookingState.from("FUTURE")).contains(BookingState.FUTURE);
    }

    @Test
    void testFromPast() {
        assertThat(BookingState.from("PAST")).contains(BookingState.PAST);
    }

    @Test
    void testFromRejected() {
        assertThat(BookingState.from("REJECTED")).contains(BookingState.REJECTED);
    }

    @Test
    void testFromWaiting() {
        assertThat(BookingState.from("WAITING")).contains(BookingState.WAITING);
    }

    @Test
    void testFromInvalid() {
        assertThat(BookingState.from("INVALID")).isEmpty();
    }
}