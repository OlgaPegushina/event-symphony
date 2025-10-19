package user.service.controller;

import interaction.api.dto.user.NewUserDto;
import interaction.api.dto.user.UpdateUserDto;
import interaction.api.dto.user.UserDto;
import interaction.api.dto.user.UserShortDto;
import interaction.api.feign.contract.UserContract;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.service.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class AdminUserController implements UserContract {
    UserService userService;

    @Override
    public UserDto createUser(@Valid @RequestBody NewUserDto newUserDto) {
        return userService.createUser(newUserDto);
    }

    @Override
    public void deleteUserById(@PathVariable @Positive Long id) {
        userService.deleteUserById(id);
    }

    @Override
    public List<UserDto> getUsers(@RequestParam(name = "ids", required = false) List<Long> userIds,
                                     @PositiveOrZero
                                     @RequestParam(name = "from", defaultValue = "0")
                                     Integer from,
                                     @Positive
                                     @RequestParam(name = "size", defaultValue = "10")
                                     Integer size) {
        return userService.getUsers(userIds, from, size);
    }

    @Override
    public UserDto updateUser(@PathVariable("userId") @Positive Long userId, @Valid @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(userId, updateUserDto);
    }

    @Override
    public UserShortDto getUserById(@PathVariable("userId") @Positive Long userId) {
        return userService.getUserById(userId);
    }
}