package dev.moon5.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",  nullable = false)
    private Board board;

    @Column(length = 255, nullable = false)
    @Setter
    private String title;

    @Setter
    private String content;

    @Setter
    private Boolean isDeleted;

    private Post(Board board, String title, String content, Boolean isDeleted) {
        this.board = board;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public static Post of(Board board, String title, String content, Boolean isDeleted) {
        return new Post(board, title, content, isDeleted);
    }

    public static Post of(Board board, String title, String content) {
        return new Post(board, title, content, false);
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

}
