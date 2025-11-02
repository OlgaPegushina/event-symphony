package analyzer.mapper;

import analyzer.model.EventSimilarity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SimilarityMapper {
    @Mapping(target = "id", ignore = true)
    EventSimilarity AvroSimilarityToEntity(EventSimilarityAvro avro);
}
