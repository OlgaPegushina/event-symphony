package analyzer.mapper;

import analyzer.model.UserAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserActionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actionWeight", source = "actionWeight")
    @Mapping(target = "actionType", ignore = true)
    UserAction AvroToEntity(UserActionAvro userActionAvro, Double actionWeight);
}
