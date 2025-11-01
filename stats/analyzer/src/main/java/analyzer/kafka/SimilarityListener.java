package analyzer.kafka;

import analyzer.service.impl.SimilarityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class SimilarityListener {
    SimilarityService similarityService;

    @KafkaListener(
            topics = "${spring.kafka.similarity-consumer.topic-events-similarity}",
            containerFactory = "similarityKafkaListenerFactory"
    )
    public void handleSimilarity(
            @Payload EventSimilarityAvro avro,
            Acknowledgment ack) {

        log.info("Получен коэффициент схожести: value={}", avro);

        try {
            similarityService.handleSimilarity(avro);
            ack.acknowledge();
            log.debug("Коэффициент схожести успешно обработан: eventA={}, eventB={}",
                    avro.getEventA(), avro.getEventB());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для коэффициента: {}, ошибка: {}", avro, e.getMessage());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке коэффициента схожести: {}", avro, e);
            ack.acknowledge();
        }
    }
}
