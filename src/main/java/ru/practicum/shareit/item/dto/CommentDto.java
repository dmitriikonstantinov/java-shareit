package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;

    private String authorName;
    private String created;
}