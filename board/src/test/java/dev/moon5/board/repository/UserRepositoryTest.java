package dev.moon5.board.repository;

import dev.moon5.board.config.JpaConfig;
import dev.moon5.board.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUserAndFindByUsername() {
        // Given
        User user = createUser();

        // When
        userRepository.save(user);

        // Then
        Optional<User> savedUser = userRepository.findByUsername(user.getUsername());
        assertThat(savedUser.isPresent()).isTrue();
        assertThat(savedUser.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void saveUserAndUpdateUser() {
        // Given
        User user = createUser();
        userRepository.save(user);
        User savedUser = userRepository.findByUsername(user.getUsername()).get();

        // When
        savedUser.setName("DummE");

        // Then
        Optional<User> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser.isPresent()).isTrue();
        assertThat(updatedUser.get().getName()).isEqualTo("DummE");
    }

    @Test
    void deleteUser() {
        // Given
        User user = createUser();
        userRepository.save(user);

        // When
        Optional<User> savedUser = userRepository.findByUsername(user.getUsername());
        assertThat(savedUser.isPresent()).isTrue();

        // Then
        savedUser.get().deleteUser();
        assertThat(savedUser.get().getIsDeleted()).isTrue();
    }

    @Test
    void testJpaAuditing() {
        // Given
        User user = createUser();
        userRepository.save(user);

        // When
        User savedUser = userRepository.findByUsername(user.getUsername()).get();

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo("ADMIN");
    }

    private User createUser() {
        return User.of("testuser", "test1234", "Dumm2");
    }

}