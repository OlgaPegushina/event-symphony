package aggregator;

import aggregator.kafka.config.AggregatorKafkaConsumerConfig;
import aggregator.kafka.producer.SimilarityProducer;
import aggregator.service.SimilarityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AggregationStarter {
    KafkaConsumer<String, UserActionAvro> consumer;
    AggregatorKafkaConsumerConfig config;
    Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    SimilarityService similarityService;

    public void start() {
        log.info("Kafka consumer успешно внедрён");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сигнал завершения получен, пробуждаем consumer");
            consumer.wakeup();
        }));

        try {
            String topic = config.getUserActionTopic();
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписка на топик: {}", topic);

            while (true) {
                log.debug("Ожидание новых сообщений");
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofSeconds(1));
                handleMessages(records);
            }

        } catch (WakeupException ignored) {
            log.info("Consumer пробуждён, выходим из цикла");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в цикле consumer", e);
        } finally {
            commitOffsets();
        }
    }

    private void handleMessages(ConsumerRecords<String, UserActionAvro> records) {
        for (ConsumerRecord<String, UserActionAvro> record : records) {
            log.debug("Получена запись: partition={}, offset={}, value={}", record.partition(), record.offset(), record.value());
            UserActionAvro userActionAvro = record.value();

            similarityService.processUserAction(userActionAvro);
            currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
        }

        if (!currentOffsets.isEmpty()) {
            consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                if (ex != null) {
                    log.error("Ошибка при коммите оффсетов {}", offsets, ex);
                }
            });
        }
    }

    private void commitOffsets() {
        try {
            log.info("Коммитим финальные оффсеты");
            consumer.commitSync(currentOffsets);
        } catch (Exception e) {
            log.error("Ошибка при коммите финальных оффсетов", e);
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
    }
}

