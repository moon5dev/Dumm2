package dev.moon5.board.dto;

import dev.moon5.board.domain.Board;

public record BoardResponseDto(
        Long id,
        String name,
        String description,
        Boolean isDeleted
) {

    public static BoardResponseDto from(Board board) {
        return new BoardResponseDto(board.getId(),
                board.getName(),
                board.getDescription(),
                board.getIsDeleted());
    }

}
