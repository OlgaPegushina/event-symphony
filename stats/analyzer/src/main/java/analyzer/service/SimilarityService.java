package analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface SimilarityService {
    void handleSimilarity(EventSimilarityAvro avro);
}
