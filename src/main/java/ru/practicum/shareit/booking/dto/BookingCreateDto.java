package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingCreateDto {
    @NotNull(message = "Дата начала не может быть пустой")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания не может быть пустой")
    @FutureOrPresent(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isStartBeforeEnd() {
        return start == null || end == null || !start.isAfter(end);
    }
}