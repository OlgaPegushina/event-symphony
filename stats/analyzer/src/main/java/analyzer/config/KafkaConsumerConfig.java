package analyzer.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@ConfigurationProperties("spring.kafka")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@SuppressWarnings("unused")
public class KafkaConsumerConfig {
    UserConsumer userConsumer = new UserConsumer();
    SimilarityConsumer similarityConsumer = new SimilarityConsumer();

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    public static class UserConsumer {
        String bootstrapServers;
        String groupId;
        String clientId;
        boolean autoCommit;
        String keyDeserializer;
        String valueDeserializer;
        String topicUserActions;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    public static class SimilarityConsumer {
        String bootstrapServers;
        String groupId;
        String clientId;
        boolean autoCommit;
        String keyDeserializer;
        String valueDeserializer;
        String topicEventsSimilarity;
    }

    @Bean
    public ConsumerFactory<String, UserActionAvro> userConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, userConsumer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, userConsumer.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, userConsumer.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, userConsumer.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, userConsumer.getValueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, userConsumer.isAutoCommit());

        log.debug("Создание userConsumerFactory с конфигурацией: {}", props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "userActionKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        log.debug("Создание userActionKafkaListenerFactory для чтения сообщений на темы: {}",
                userConsumer.getTopicUserActions());

        return factory;
    }

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> similarityConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, similarityConsumer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, similarityConsumer.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, similarityConsumer.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, similarityConsumer.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, similarityConsumer.getValueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, similarityConsumer.isAutoCommit());

        log.debug("Создание similarityConsumerFactory с конфигурацией: {}", props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "similarityKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> similarityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(similarityConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        log.debug("Создание similarityKafkaListenerFactory для чтения сообщений на темы: {}",
                similarityConsumer.getTopicEventsSimilarity());

        return factory;
    }
}

