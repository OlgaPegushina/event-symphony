package analyzer.service.impl;

import analyzer.mapper.SimilarityMapper;
import analyzer.model.EventSimilarity;
import analyzer.repository.EventSimilarityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SimilarityService implements analyzer.service.SimilarityService {
    EventSimilarityRepository similarityRepository;
    SimilarityMapper similarityMapper;

    @Override
    public void handleSimilarity(EventSimilarityAvro avro) {
        log.info("Создание схожести события: {}", avro);

        if (similarityRepository.existsByEventAAndEventB(avro.getEventA(), avro.getEventB())) {
            log.debug("Запись с eventA={} и eventB={} уже есть, пропускаем",
                    avro.getEventA(), avro.getEventB());
            return;
        }

        EventSimilarity similarity = similarityMapper.AvroSimilarityToEntity(avro);
        similarityRepository.save(similarity);
    }
}