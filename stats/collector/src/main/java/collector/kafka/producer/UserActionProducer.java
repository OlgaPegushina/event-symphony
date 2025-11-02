package collector.kafka.producer;

import collector.kafka.config.StatsKafkaProducerConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionProducer {
    KafkaTemplate<String, Object> kafkaTemplate;
    StatsKafkaProducerConfig config;

    public void sendUserAction(UserActionAvro userAction) {
        String topicName = config.getUserActionTopic();

        log.info("В топик: {} отправляется сообщение: {}", topicName, userAction);
        kafkaTemplate.send(topicName, userAction)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Сообщение о действии пользователя: {} успешно отправлено, смещение: {}",
                                userAction, result.getRecordMetadata().offset());
                    } else {
                        log.error("Не удалось отправить сообщение о действии пользователя: {} : {}",
                                userAction, exception.getMessage());
                    }
                });
    }
}