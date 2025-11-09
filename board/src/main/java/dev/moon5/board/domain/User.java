package dev.moon5.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
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
    private String role;

    @Setter
    private Boolean isDeleted;

    private User(String username, String password, String name, String role, Boolean isDeleted) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.isDeleted = isDeleted;
    }

    public static User of(String username,
                          String password,
                          String name,
                          String role,
                          Boolean isDeleted) {
        return new User(username,
                password,
                name,
                role,
                isDeleted);
    }

    public static User of(String username,
                          String password,
                          String name,
                          String role) {
        return new User(username,
                password,
                name,
                role,
                false);
    }

    public static User of(String username,
                          String password,
                          String name) {
        return new User(username,
                password,
                name,
                "USER",
                false);
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeRole(String role) {
        this.role = role;
    }

    public void deleteUser() {
        this.isDeleted = true;
    }

}
