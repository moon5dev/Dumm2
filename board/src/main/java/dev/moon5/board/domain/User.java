package dev.moon5.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    @Setter
    private String username;

    @Column(length = 255, nullable = false)
    @Setter
    private String password;

    @Column(length = 100, nullable = false)
    @Setter
    private String name;

    @Column(length = 20)
    @Setter
    private String role = "USER";

    @Setter
    private Boolean isDelete = false;

}
