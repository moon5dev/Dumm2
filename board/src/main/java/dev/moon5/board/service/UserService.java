package dev.moon5.board.service;

import dev.moon5.board.domain.User;
import dev.moon5.board.dto.UserRegisterDto;
import dev.moon5.board.dto.UserResponseDto;
import dev.moon5.board.dto.UserUpdateDto;
import dev.moon5.board.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto register(UserRegisterDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User savedUser = userRepository.save(dto.toEntity());

        return UserResponseDto.from(savedUser);
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found, id = " + id));

        return UserResponseDto.from(user);
    }

    public Page<UserResponseDto> findAll(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        return users.map(UserResponseDto::from);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found, id = " + id));

        if (dto.password() != null && !dto.password().isEmpty()) user.changePassword(dto.password());
        if (dto.name() != null && !dto.name().isEmpty()) user.changeName(dto.name());
        if (dto.role() != null && !dto.role().isEmpty()) user.changeRole(dto.role());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found, id = " + id));

        user.deleteUser();
    }

}
