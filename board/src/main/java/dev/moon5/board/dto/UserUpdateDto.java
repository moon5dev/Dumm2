package dev.moon5.board.dto;

public record UserUpdateDto(
        String password,
        String name,
        String role
) {
}
