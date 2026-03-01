package dev.moon5.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Board extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    @Setter
    private String name;

    @Column(length = 500)
    @Setter
    private String description;

    private Boolean isDeleted;

    private Board(String name, String description, Boolean isDeleted) {
        this.name = name;
        this.description = description;
        this.isDeleted = isDeleted;
    }

    public static Board of(String name, String description, Boolean isDeleted) {
        return new Board(name, description, isDeleted);
    }

    public static Board of(String name, String description) {
        return new Board(name, description, false);
    }

    public void changeDescription(String description) {
        this.description = description;
    }

}
