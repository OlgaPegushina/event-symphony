package main.service.compilation;

import lombok.Getter;
import main.service.compilation.dto.CompilationDto;
import main.service.compilation.dto.NewCompilationDto;
import main.service.compilation.model.Compilation;
import main.service.events.mapper.EventMapper;
import main.service.events.model.EventModel;
import main.service.events.services.impls.PrivateServiceImpl;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "events", target = "events", qualifiedByName = "mapEventIds")
    Compilation toEntity(NewCompilationDto dto, @Context MapperContext context);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDtoList(List<Compilation> compilations);

    @Named("mapEventIds")
    default Set<EventModel> map(Set<Long> eventIds, @Context MapperContext context) {
        if (eventIds == null) {
            return null;
        }
        return eventIds.stream()
                .map(id -> mapToEventModel(id, context))
                .collect(Collectors.toSet());
    }

    default EventModel mapToEventModel(Long eventId, @Context MapperContext context) {
        return (context != null && context.getEvenService() != null)
                ? context.getEvenService().findById(eventId).orElse(null)
                : null;
    }

    @Getter
    class MapperContext {
        private final PrivateServiceImpl evenService;

        public MapperContext(PrivateServiceImpl evenService) {
            this.evenService = evenService;
        }
    }
}

