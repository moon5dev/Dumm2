package dev.moon5.board.repository;

import dev.moon5.board.config.JpaConfig;
import dev.moon5.board.domain.Board;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    void saveTest() {
        // Given
        Board board = createBoard();

        // When
        Board savedBoard = boardRepository.save(board);

        // Then
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getName()).isEqualTo(board.getName());
    }

    private Board createBoard() {
        return Board.of("Test board", "This is a test board.");
    }

}