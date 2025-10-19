package user.service.mapper;

import interaction.api.dto.user.NewUserDto;
import interaction.api.dto.user.UserDto;
import interaction.api.dto.user.UserShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import user.service.model.User;

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
