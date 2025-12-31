package dev.moon5.board.dto;

import dev.moon5.board.domain.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String username,
        String name,
        String role,
        Boolean isDeleted,
        LocalDateTime createdDate
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.getIsDeleted(),
                user.getCreatedDate());
    }

}
