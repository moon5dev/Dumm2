package dev.moon5.board.dto;

import dev.moon5.board.domain.Board;

public record BoardCreateDto(
        String name,
        String description
) {

    public Board toEntity() {
        return Board.of(name,
                description);
    }

}
