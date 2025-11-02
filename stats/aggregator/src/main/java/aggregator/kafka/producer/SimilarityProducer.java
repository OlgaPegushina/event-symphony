package aggregator.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

@Component
@Slf4j
public class SimilarityProducer {
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    private final String topicName;

    public SimilarityProducer(KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate,
                              @Value("${spring.kafka.producer.topic.events-similarity}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

   public void sendSimilarityScores(List<EventSimilarityAvro> messages) {
       log.debug("Отправка {} сообщений в топик '{}'", messages.size(), topicName);
       for (EventSimilarityAvro message : messages) {
           send(message);
       }
   }
    private void send(EventSimilarityAvro message) {
        kafkaTemplate.send(topicName, message)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Оценка сходства успешно отправлена: message={}", message);
                    } else {
                        log.error("Ошибка при отправке оценки сходства в топик '{}': message={}",
                                topicName, message, exception);
                    }
                });
    }
}
