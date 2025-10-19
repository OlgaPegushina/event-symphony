package interaction.api.feign.contract;

import interaction.api.dto.user.NewUserDto;
import interaction.api.dto.user.UpdateUserDto;
import interaction.api.dto.user.UserDto;
import interaction.api.dto.user.UserShortDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public interface UserContract {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto createUser(@Valid @RequestBody NewUserDto newUserDto);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUserById(@PathVariable @Positive Long id);

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<UserDto> getUsers(@RequestParam(name = "ids", required = false) List<Long> userIds,
                                  @PositiveOrZero
                                  @RequestParam(name = "from", defaultValue = "0")
                                  Integer from,
                                  @Positive
                                  @RequestParam(name = "size", defaultValue = "10")
                                  Integer size);

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    UserDto updateUser(@PathVariable("userId") @Positive Long userId, @Valid @RequestBody UpdateUserDto updateUserDto);

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    UserShortDto getUserById(@PathVariable("userId") @Positive Long userId);
}
