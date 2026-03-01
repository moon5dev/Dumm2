package dev.moon5.board.repository;

import dev.moon5.board.config.JpaConfig;
import dev.moon5.board.domain.Board;
import dev.moon5.board.domain.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void savePostWithBoard() {
        // Given
        Board board = createBoard();
        Post post = createPost(board);

        // When
        boardRepository.save(board);
        postRepository.save(post);

        // Then
        assertThat(postRepository.findAll()).hasSize(1);
    }

    private Board createBoard() {
        return Board.of("Test board", "This is a test board.");
    }

    private Post createPost(Board board) {
        return Post.of(board, "Test post", "This is a test post.");
    }

}