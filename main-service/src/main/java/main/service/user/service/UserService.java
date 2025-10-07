package main.service.user.service;

import main.service.user.dto.NewUserDto;
import main.service.user.dto.UpdateUserDto;
import main.service.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    void deleteUserById(Long userId);

    List<UserDto> getUsers(List<Long> userIds, int from, int size);

    UserDto updateUser(Long userId, UpdateUserDto updateUserDto);


}