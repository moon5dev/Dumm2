package dev.moon5.board.dto;

public record BoardUpdateDto(
        String name,
        String description,
        Boolean isDeleted
) {
}
