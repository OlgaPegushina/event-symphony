package main.service.location;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto locationDto);
}
