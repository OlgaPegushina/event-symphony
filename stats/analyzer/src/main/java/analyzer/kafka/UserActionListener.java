package analyzer.kafka;

import analyzer.service.UserActionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class UserActionListener {
    UserActionService userActionService;

    @KafkaListener(
            topics = "${spring.kafka.user-consumer.topic-user-actions}",
            containerFactory = "userActionKafkaListenerFactory"
    )
    public void handleUserAction(
            @Payload UserActionAvro avro,
            Acknowledgment ack) {

        log.info("Получено действие пользователя: value={}", avro);

        try {
            userActionService.handleUserAction(avro);
            ack.acknowledge();
            log.debug("Действие пользователя успешно обработано: userId={}, eventId={}",
                    avro.getUserId(), avro.getEventId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для действия: {}, ошибка: {}", avro, e.getMessage());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке действия пользователя: {}", avro, e);

            ack.acknowledge();
        }
    }
}
