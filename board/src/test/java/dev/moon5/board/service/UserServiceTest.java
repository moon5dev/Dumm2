package dev.moon5.board.service;

import dev.moon5.board.domain.User;
import dev.moon5.board.dto.UserRegisterDto;
import dev.moon5.board.dto.UserResponseDto;
import dev.moon5.board.dto.UserUpdateDto;
import dev.moon5.board.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService sut;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser() {
        // Given
        UserRegisterDto dto = createRegisterDto();

        // When
        UserResponseDto responseDto = sut.register(dto);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.name()).isEqualTo(dto.name());
    }

    @Test
    void findById() {
        // Given
        User user = createUser();
        User savedUser = userRepository.save(user);

        // When
        UserResponseDto responseDto = sut.findById(savedUser.getId());

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.username()).isEqualTo(user.getUsername());
    }

    @Test
    void findAllUsers() {
        // Given
        for (int i = 0; i < 10; i++) {
            User user = User.of("test_user" + i, "test1234", "Dumm2");
            userRepository.save(user);
        }

        // When
        Page<UserResponseDto> users = sut.findAll(PageRequest.of(0, 10));

        // Then
        assertThat(users).isNotNull();
        assertThat(users.getTotalElements()).isEqualTo(10);
    }

    @Test
    void updateUser() {
        // Given
        User user = createUser();
        User savedUser = userRepository.save(user);
        UserUpdateDto updateDto = new UserUpdateDto("test1234", "DummE", null);

        // When
        sut.updateUser(savedUser.getId(), updateDto);

        // Then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo(updateDto.name());
        assertThat(updatedUser.getPassword()).isEqualTo(updateDto.password());
    }

    @Test
    void deleteUser() {
        // Given
        User user = createUser();
        User savedUser = userRepository.save(user);

        // When
        sut.deleteUser(savedUser.getId());

        // Then
        User deletedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(deletedUser.getIsDeleted()).isTrue();
    }

    private UserRegisterDto createRegisterDto() {
        return new UserRegisterDto("test_user",
                "test1234",
                "Dumm2",
                "ADMIN");
    }

    private User createUser() {
        return User.of("test_user",
                "test1234",
                "Dumm2");
    }

}