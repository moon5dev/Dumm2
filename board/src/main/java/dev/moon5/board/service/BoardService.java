package dev.moon5.board.service;

import dev.moon5.board.domain.Board;
import dev.moon5.board.dto.BoardCreateDto;
import dev.moon5.board.dto.BoardResponseDto;
import dev.moon5.board.dto.BoardUpdateDto;
import dev.moon5.board.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public BoardResponseDto create(BoardCreateDto dto) {
        Board board = dto.toEntity();

        Board savedBoard = boardRepository.save(board);

        return BoardResponseDto.from(savedBoard);
    }

    @Transactional
    public BoardResponseDto update(Long id, BoardUpdateDto dto) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found, id = " + id));

        if (dto.name() != null) board.setName(dto.name());
        if (dto.description() != null) board.setDescription(dto.description());
        if (dto.isDeleted() != null) board.setIsDeleted(dto.isDeleted());

        return  BoardResponseDto.from(board);
    }

    public BoardResponseDto findById(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found, id = " + id));

        return BoardResponseDto.from(board);
    }

    public Page<BoardResponseDto> findAll(Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);

        return boards.map(BoardResponseDto::from);
    }

    public void delete(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Board not found, id = " + id));

        board.setIsDeleted(true);
    }

}
