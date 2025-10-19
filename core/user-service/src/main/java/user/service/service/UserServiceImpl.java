package user.service.service;

import interaction.api.dto.user.NewUserDto;
import interaction.api.dto.user.UpdateUserDto;
import interaction.api.dto.user.UserDto;
import interaction.api.dto.user.UserShortDto;
import interaction.api.exception.DuplicatedDataException;
import interaction.api.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.service.mapper.UserMapper;
import user.service.repository.UserRepository;
import user.service.model.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserDto newUserDto) {
        validateEmailExist(newUserDto.getEmail());
        log.info("создаем User {}", newUserDto.getName());
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(newUserDto)));
    }

    @Override
    public void deleteUserById(Long userId) {
        validateUserExist(userId);
        log.info("удаляем User id={}", userId);
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> userIds, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<User> usersPage;
        if (userIds == null || userIds.isEmpty()) {
            usersPage = userRepository.findAll(pageable);
        } else {
            usersPage = userRepository.findByIdIn(userIds, pageable);
        }
        return userMapper.toUserDtoPage(usersPage.getContent(), pageable).getContent();
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUserDto updateUserDto) {
        User user = validateUserExist(userId);
        validateEmailExist(updateUserDto.getEmail(), userId);
        updateUserFields(user, updateUserDto);
        log.info("обновляем информацию о User id={}", userId);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserShortDto getUserById(Long userId) {
        return userMapper.toShortDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id= %d не найден.", userId))));
    }

/*    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }*/

    private void validateEmailExist(String email, Long currentUserId) {
        Optional<User> alreadyExistUser = userRepository.findByEmail(email);
        if (alreadyExistUser.isPresent() && !alreadyExistUser.get().getId().equals(currentUserId)) {
            throw new DuplicatedDataException(String.format("Email - %s уже используется", email));
        }
    }

    private void validateEmailExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicatedDataException(String.format("Email - %s уже используется", email));
        }
    }

    private User validateUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id= %d не найден.", userId)));
    }

    private void updateUserFields(User user, UpdateUserDto updateUserDto) {
        if (updateUserDto.hasEmail()) {
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.hasName()) {
            user.setName(updateUserDto.getName());
        }
    }
}
