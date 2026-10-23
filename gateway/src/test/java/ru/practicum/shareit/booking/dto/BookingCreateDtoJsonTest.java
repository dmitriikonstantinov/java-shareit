package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testSerializeBookingCreateDto() throws Exception {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setItemId(1L);

        var result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }

    @Test
    void testDeserializeBookingCreateDto() throws Exception {
        String content = "{\"start\":\"2026-10-25T10:00:00\",\"end\":\"2026-10-26T10:00:00\",\"itemId\":1}";
        BookingCreateDto dto = json.parseObject(content);
        assertThat(dto.getItemId()).isEqualTo(1L);
    }
}