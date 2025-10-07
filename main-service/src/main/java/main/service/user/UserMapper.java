package main.service.user;

import main.service.user.dto.NewUserDto;
import main.service.user.dto.UserDto;
import main.service.user.dto.UserShortDto;
import main.service.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toUser(NewUserDto newUserDto);

    UserDto toUserDto(User user);

    UserShortDto toShortDto(User user);

    User toUser(UserDto userDto);

    default Page<UserDto> toUserDtoPage(List<User> users, Pageable pageable) {
        List<UserDto> userDtos = users == null ? List.of() : users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
        return new PageImpl<>(userDtos, pageable, userDtos.size());
    }
}
