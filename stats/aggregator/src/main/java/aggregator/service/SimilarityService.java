package aggregator.service;

import aggregator.kafka.config.AggregatorProperties;
import aggregator.kafka.producer.SimilarityProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Map;

/**
 * Сервис, отвечающий за расчет и обновление сходства между событиями
 * на основании действий пользователя.
 * Принцип работы:
 * 1. Получает сообщения о действиях пользователей из Kafka.
 * 2. Определяет вес для каждого действия.
 * 3. Обновляет внутреннее состояние (максимальные веса пользователей, суммы квадратов весов событий) и т.д.
 * 4. Пересчитывает сходство между текущим событием и другими событиями,
 *    с которыми пользователь взаимодействовал.
 * 5. Отправляет обновленные значения сходства обратно в Kafka.
 */
@Service
@Slf4j
public class SimilarityService {
    /**
     * Состояние state:
     * - Максимальный вес взаимодействия пользователя с событием (userEventWeight).
     * - Сумма квадратов весов для каждого события (eventSquaredWeightSum).
     * - Сумма минимальных весов для пары событий (minWeightsPairSum).
     * - Множество событий, с которыми взаимодействовал пользователь (eventsByUser).
     */
    private final SimilarityState state;

    /**
     * Продюсер - отправка рассчитанных значений сходства в Kafka топик.
     */
    private final SimilarityProducer producer;

    /**
     * Карта весов для различных типов действий пользователя.
     * Получается из {@link AggregatorProperties} и внедряется через конструктор.
     * Ключ: тип действия ({@link ActionTypeAvro}).
     * Значение: соответствующий вес Double.
     */
    private final Map<ActionTypeAvro, Double> actionWeights;

    public SimilarityService(SimilarityState state, SimilarityProducer producer, AggregatorProperties properties) {
        this.state = state;
        this.producer = producer;
        // -- Загружаем карту весов из внедренных свойств
        this.actionWeights = properties.getWeights();

        log.debug("Инициализация SimilarityService. Загружены веса: {}", this.actionWeights);
    }

    /**
     * Основной метод для обработки действия пользователя, полученного из Kafka.
     */
    public void processUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        // Определяем вес для текущего действия пользователя.
        double newWeight = getWeightForAction(action.getActionType());

        // Если вес нулевой (для неизвестного типа действия),
        // то нет смысла продолжать обработку, выходим.
        if (newWeight == 0.0) {
            log.warn("Получено действие с нулевым весом (неизвестным типом): {} от пользователя {} для события {}. Игнорируем.",
                    action.getActionType(), userId, eventId);
            return;
        }

        // Получаем предыдущий максимальный вес, с которым пользователь взаимодействовал с этим событием.
        // Если пользователь взаимодействует с событием впервые, oldWeight будет 0.0 (из SimilarityState).
        double oldWeight = state.getUserEventWeight(userId, eventId);

        // Если новый вес не больше старого, то это действие не увеличивает значимость пользователя для этого события.
        // Поэтому нет необходимости пересчитывать сходства, выходим
        if (newWeight <= oldWeight) {
            log.trace("Новый вес ({}) не превосходит старый ({}) для пользователя {} и события {}. Обработка пропущена.",
                    newWeight, oldWeight, userId, eventId);
            return;
        }

        log.debug("Обработка значимого действия: пользователь={}, событие={}, новый вес={}, старый вес={}",
                userId, eventId, newWeight, oldWeight);

        // Обновляем максимальный вес для данной пары (пользователь, событие)
        state.updateUserEventWeight(userId, eventId, newWeight);

        // Обновляем сумму весов для текущего события (SA), прибавляем разницу квадратов delta
        double deltaWeight = (newWeight - oldWeight);
        log.debug("Посчитали разницу delta: {}", deltaWeight);
        state.updateEventWeightSum(eventId, deltaWeight);

        // Бежим по всем другим событиям, с которыми этот пользователь уже взаимодействовал.
        // Для каждого такого события нужно пересчитать сходство с текущим событием eventId.
        for (Long otherEventId : state.getEventsByUser(userId)) {
            // Пропускаем сравнение события с самим собой.
            if (otherEventId.equals(eventId)) {
                continue;
            }

            // Получаем вес пользователя для другого события.
            double otherEventWeight = state.getUserEventWeight(userId, otherEventId);
            log.debug("Получили вес пользователя для сравниваемого события: {}", otherEventWeight);

            // Обновляем сумму минимальных весов для пары (eventId, otherEventId).
            double deltaSMin = Math.min(newWeight, otherEventWeight) - Math.min(oldWeight, otherEventWeight);
            log.debug("Обновляемая сумма deltaSMin: {} минимальных весов для пары (eventId: {}, otherEventId: {}): , при " +
                      "oldWeight: {}, newWeight: {}, otherEventWeight: {}",
                    deltaSMin, eventId, otherEventId, oldWeight, newWeight, otherEventWeight);

            // Если дельта равна 0, значит, минимальное значение не изменилось,
            // и сходство между этой парой событий оставляем прежним.
            // Пропускаем пересчет для этой пары.
            if (deltaSMin == 0.0) {
                log.trace("Дельта Smin равна 0 для пары ({}, {}). Пропуск пересчета.", eventId, otherEventId);
                continue;
            }

            // Применяем дельту к сумме минимальных весов для пары.
            state.updateMinWeightsPairSum(eventId, otherEventId, deltaSMin);

            // Пересчитываем и отправляем новое значение сходства для пары (eventId, otherEventId).
            // также передаем временную метку из действия пользователя, пригодится
            recalculateAndSendSimilarity(eventId, otherEventId, action.getTimestamp());
        }
    }

    /**
     * Пересчитывает значение сходства для пары событий (eventA, eventB)
     * и отправляет его в Kafka.
     *
     * @param eventA    Id первого события.
     * @param eventB    Id второго события.
     * @param timestamp Временная метка действия, вызвавшего пересчет.
     */
    private void recalculateAndSendSimilarity(long eventA, long eventB, Instant timestamp) {
        log.info("Получаем актуальные значения для расчета сходства из состояния");
        // Получаем актуальные значения для расчета сходства из состояния:
        // Smin(A, B) - сумма минимальных весов для пары (A, B)
        double sMin = state.getMinWeightsPairSum(eventA, eventB);
        log.debug("Smin(A, B): {} для пары ({}, {})",sMin, eventA, eventB);
        // SA - сумма весов для события A
        double sA = state.getEventWeightSum(eventA);
        log.debug("сумма весов для события A: {}", sA);
        // SB - сумма весов для события B
        double sB = state.getEventWeightSum(eventB);
        log.debug("сумма весов для события B: {}", sB);

        // Рассчитываем итоговое значение сходства по формуле косинусного сходства:
        // score = Smin(A, B) / (sqrt(SA) * sqrt(SB))
        double score = 0.0;
        // Проверяем, что знаменатель не равен нулю, чтобы избежать деления на ноль.
        if (sA > 0 && sB > 0) {
            double denominator = Math.sqrt(sA) * Math.sqrt(sB);
            if (denominator > 0) {
                score = sMin / denominator;
            }
        }

        log.debug("Пересчитано сходство для пары ({}, {}): {}", eventA, eventB, score);

        // Отправляем рассчитанное значение сходства в Kafka.
        producer.sendSimilarityScore(eventA, eventB, score, timestamp);
    }

    /**
     * Получает числовой вес, соответствующий данному типу действия пользователя или 0, если тип действия неизвестен
     */
    private double getWeightForAction(ActionTypeAvro actionType) {
        // Используем полученную из конфигурации карту actionWeights.
        return this.actionWeights.getOrDefault(actionType, 0.0);
    }
}