package request.service.mapper;

import interaction.api.dto.request.ParticipationRequestDto;
import request.service.model.ParticipationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(source = "eventId", target = "eventId")
    @Mapping(source = "requesterId", target = "requesterId")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);
}
