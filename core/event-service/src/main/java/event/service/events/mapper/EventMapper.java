package event.service.events.mapper;

import event.service.category.mapper.CategoryMapper;
import event.service.category.model.Category;
import event.service.events.model.EventModel;
import event.service.location.Location;
import event.service.location.LocationMapper;
import interaction.api.dto.event.EventFullDto;
import interaction.api.dto.event.EventShortDto;
import interaction.api.dto.event.NewEventDto;
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
    @Mapping(target = "state",             constant = "PENDING")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "createdOn",         expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn",       ignore = true)
    @Mapping(target = "category",  source = "category")
    @Mapping(target = "initiatorId", source = "userId")
    @Mapping(target = "location",  source = "location")
    EventModel toEntity(NewEventDto dto,
                        Category category,
                        Long userId,
                        Location location);

    @Mapping(source = "category", target = "categoryDto")
    @Mapping(source = "location", target = "locationDto")
    @Mapping(source = "initiatorId", target = "initiator")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventFullDto toFullDto(EventModel entity);

    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiatorId", target = "initiator")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventShortDto toShortDto(EventModel entity);
}
