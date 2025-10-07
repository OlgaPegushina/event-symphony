package main.service.request;

import main.service.request.dto.ParticipationRequestDto;
import main.service.request.model.ParticipationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "requester.id", target = "requesterId")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);
}
