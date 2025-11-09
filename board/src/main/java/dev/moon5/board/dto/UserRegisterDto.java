package dev.moon5.board.dto;

import dev.moon5.board.domain.User;

public record UserRegisterDto(
        String username,
        String password,
        String name,
        String role
) {

    public User toEntity() {
        return User.of(username,
                password,
                name,
                role);
    }

}
