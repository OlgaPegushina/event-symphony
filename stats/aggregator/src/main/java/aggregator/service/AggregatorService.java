package aggregator.service;

import aggregator.kafka.config.AggregatorProperties;
import aggregator.kafka.producer.SimilarityProducer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для агрегации данных о действиях пользователей и расчета схожести событий.
 * Этот сервис принимает события действий пользователей, обновляет веса действий
 * и пересчитывает схожесть между событиями на основе этих весов.
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AggregatorService {
    // -- Хранит веса действий пользователя для каждого события.
    // -- Структура: {eventId -> {userId -> weight}}
    Map<Long, Map<Long, Double>> eventUserWeights;

    // -- Хранит скалярные произведения между событиями.
    // -- Используется для вычисления схожести.
    // -- Структура: {eventId -> {otherEventId -> dotProduct}}
    Map<Long, Map<Long, Double>> scalarResultMatrix;

    SimilarityProducer producer;

    // -- Веса для различных типов действий, полученные из конфигурации.
    // -- Структура: {ActionTypeAvro -> weight}
    Map<ActionTypeAvro, Double> actionWeights;

    public AggregatorService(SimilarityProducer producer, AggregatorProperties properties) {
        this.producer = producer;
        this.actionWeights = properties.getWeights(); // Получаем веса из properties

        this.eventUserWeights = new HashMap<>();
        this.scalarResultMatrix = new HashMap<>();

        log.info("AggregationService инициализирован с весами: {}", this.actionWeights);
    }

    // -- обновляет вес действия для данного пользователя и события,
    // -- затем пересчитывает и отправляет обновленные оценки схожести событий.
    public void calculateSimilarity(UserActionAvro request) {
        double newWeight = getWeight(request.getActionType());

        List<EventSimilarityAvro> similarities = updateEventWeight(
                request.getEventId(),
                request.getUserId(),
                newWeight
        );

        producer.sendSimilarityScores(similarities.stream()
                .sorted(Comparator.comparingLong(EventSimilarityAvro::getEventA)
                        .thenComparingLong(EventSimilarityAvro::getEventB))
                .collect(Collectors.toList()));
    }

    private double getWeight(ActionTypeAvro actionType) {
        return this.actionWeights.getOrDefault(actionType, 0.0);
    }

    // -- Обновляет вес действия для конкретного пользователя и события.
    // -- Если новый вес больше текущего, запускает пересчет схожестей.
    private List<EventSimilarityAvro> updateEventWeight(Long eventId, Long userId, Double newWeight) {
        Map<Long, Double> userWeights = eventUserWeights.computeIfAbsent(eventId, k -> new HashMap<>());
        Double currentWeight = userWeights.get(userId);

        if (currentWeight == null || currentWeight < newWeight) {
            List<EventSimilarityAvro> updatedSimilarities = recalculateSimilarities(
                    eventId,
                    userId,
                    newWeight,
                    currentWeight
            );
            userWeights.put(userId, newWeight);
            return updatedSimilarities;
        }
        return Collections.emptyList();
    }

    private List<EventSimilarityAvro> recalculateSimilarities(Long eventId, Long userId,
                                                              Double newWeight, Double oldWeight) {
        Map<Long, Double> selfDotProducts = scalarResultMatrix.computeIfAbsent(eventId, k -> new HashMap<>());
        double currentSelfProduct = selfDotProducts.getOrDefault(eventId, 0.0);
        double weightDelta = (oldWeight == null) ? newWeight : newWeight - oldWeight;

        selfDotProducts.put(eventId, currentSelfProduct + weightDelta);

        return updateCrossDotProducts(eventId, userId, newWeight, oldWeight);
    }

    // -- Обновляет "перекрестные" скалярные произведения (dot products) между обновленным событием
    // -- и всеми другими событиями.
    private List<EventSimilarityAvro> updateCrossDotProducts(Long updatedEventId, Long userId,
                                                             Double newWeight, Double oldWeight) {
        List<EventSimilarityAvro> updatedSimilarities = new ArrayList<>();

        for (Long otherEventId : eventUserWeights.keySet()) {
            if (updatedEventId.equals(otherEventId)) continue;

            long eventA, eventB;
            boolean isUpdatedFirst;
            if (updatedEventId < otherEventId) {
                eventA = updatedEventId;
                eventB = otherEventId;
                isUpdatedFirst = true;
            } else {
                eventA = otherEventId;
                eventB = updatedEventId;
                isUpdatedFirst = false;
            }

            Map<Long, Double> otherUserWeights = eventUserWeights.get(otherEventId);
            if (otherUserWeights != null) {
                Double otherWeight = otherUserWeights.get(userId);
                if (otherWeight != null) {
                    EventSimilarityAvro similarity = updateDotProductForPair(
                            eventA, eventB, newWeight, oldWeight, otherWeight, isUpdatedFirst
                    );
                    if (similarity != null) {
                        updatedSimilarities.add(similarity);
                    }
                }
            }
        }
        return updatedSimilarities;
    }

    // -- Обновляет скалярное произведение для пары событий (eventA, eventB) и рассчитывает новую схожесть.
    // -- Алгоритм основан на обновлении минимальных весов, которые определяют пересечение.
    private EventSimilarityAvro updateDotProductForPair(long eventA, long eventB,
                                                        Double newWeight, Double oldWeight,
                                                        Double otherWeight, boolean isUpdatedFirst) {
        Map<Long, Double> dotProducts = scalarResultMatrix.computeIfAbsent(eventA, k -> new HashMap<>());

        double currentDotProduct = dotProducts.getOrDefault(eventB, 0.0);
        double oldMinWeight = (oldWeight == null) ? 0.0 : Math.min(oldWeight, otherWeight);
        double weightForMin;
        if (isUpdatedFirst) {
            weightForMin = newWeight;
        } else {
            weightForMin = otherWeight;
        }
        double newMinWeight = Math.min(weightForMin, isUpdatedFirst ? otherWeight : newWeight);

        double dotProductDelta = newMinWeight - oldMinWeight;

        double updatedDotProduct = currentDotProduct + dotProductDelta;
        dotProducts.put(eventB, updatedDotProduct);

        return calculateSimilarity(eventA, eventB, updatedDotProduct);
    }

    // -- Рассчитывает схожесть между двумя событиями
    private EventSimilarityAvro calculateSimilarity(long eventA, long eventB, double dotProduct) {
        Double normA = calculateNorm(eventA);
        Double normB = calculateNorm(eventB);

        if (normA == null || normB == null || normA == 0 || normB == 0) {
            return null;
        }

        double similarity = dotProduct / (normA * normB);
        return new EventSimilarityAvro(eventA, eventB, similarity, Instant.now());
    }

    private Double calculateNorm(Long eventId) {
        Map<Long, Double> selfDotProducts = scalarResultMatrix.get(eventId);
        if (selfDotProducts == null) return null;

        Double selfProduct = selfDotProducts.get(eventId);
        return (selfProduct != null) ? Math.sqrt(selfProduct) : null;
    }
}
