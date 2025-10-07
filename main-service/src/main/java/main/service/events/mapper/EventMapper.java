package main.service.events.mapper;

import main.service.category.mapper.CategoryMapper;
import main.service.category.model.Category;
import main.service.events.dto.EventFullDto;
import main.service.events.dto.EventShortDto;
import main.service.events.dto.NewEventDto;
import main.service.events.model.EventModel;
import main.service.location.Location;
import main.service.location.LocationMapper;
import main.service.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = { CategoryMapper.class, LocationMapper.class })
public interface EventMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "annotation",       source = "dto.annotation")
    @Mapping(target = "description",      source = "dto.description")
    @Mapping(target = "eventDate",        source = "dto.eventDate")
    @Mapping(target = "paid",             source = "dto.paid")
    @Mapping(target = "participantLimit", source = "dto.participantLimit")
    @Mapping(target = "requestModeration",source = "dto.requestModeration")
    @Mapping(target = "title",            source = "dto.title")

    // фиксируем первоначальные поля
    @Mapping(target = "state",             constant = "PENDING")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "createdOn",         expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn",       ignore = true)

    // объекты
    @Mapping(target = "category",  source = "category")
    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "location",  source = "location")
    EventModel toEntity(NewEventDto dto,
                        Category category,
                        User user,
                        Location location);

    @Mapping(source = "category", target = "categoryDto")
    @Mapping(source = "location", target = "locationDto")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(target = "views", ignore = true)
    EventFullDto toFullDto(EventModel entity);

    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(EventModel entity);
}
