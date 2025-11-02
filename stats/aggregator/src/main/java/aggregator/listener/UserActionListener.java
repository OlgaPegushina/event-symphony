package aggregator.listener;

import aggregator.service.AggregatorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class UserActionListener {
    AggregatorService similarityService;

    @KafkaListener(
            topics = "${spring.kafka.consumer.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserAction(UserActionAvro message) {
        log.debug("Получено сообщение из Kafka: {}", message);
        try {
            similarityService.calculateSimilarity(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", message, e);
        }
    }
}
