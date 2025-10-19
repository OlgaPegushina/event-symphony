package user.service.service;

import interaction.api.dto.user.NewUserDto;
import interaction.api.dto.user.UpdateUserDto;
import interaction.api.dto.user.UserDto;
import interaction.api.dto.user.UserShortDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    void deleteUserById(Long userId);

    List<UserDto> getUsers(List<Long> userIds, int from, int size);

    UserDto updateUser(Long userId, UpdateUserDto updateUserDto);

    UserShortDto getUserById(Long userId);
}