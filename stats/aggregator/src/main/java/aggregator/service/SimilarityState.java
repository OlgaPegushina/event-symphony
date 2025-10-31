package aggregator.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс, отвечающий за хранение и обновление состояния, необходимого для расчета сходства событий.
 * Это состояние включает:
 * - Максимальный вес взаимодействия пользователя с конкретным событием (userEventWeight).
 * - Сумму весов всех действий для каждого события (eventWeightSum).
 * - Сумму минимальных весов для пар событий (minWeightsPairSum).
 * - Множество событий, с которыми взаимодействовал пользователь (eventsByUser).
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SimilarityState {
    /**
     * Хранит максимальный вес действия каждого пользователя для каждого события.
     * Ключ: строка вида "userId:eventId".
     */
    Map<String, Double> userEventWeight = new ConcurrentHashMap<>();

    /**
     * Сумма квадратов весов: Map<EventId, Double>
     * SА = сумма (weight_i в квадрате) для всех действий i, связанных с событием A.
     * Ключ: eventId.
     * Значение: Сумма квадратов весов.
     */
    Map<Long, Double> eventWeightSum = new ConcurrentHashMap<>();

    /**
     * Сумма минимальных весов для для каждой пары событий.
     * Хранит Smin(A, B) = сумма (min(weight(user, A), weight(user, B)))
     * для всех пользователей, взаимодействовавших с обоими событиями A и B.
     * Ключ: Пара событий (eventIdA, eventIdB), где eventIdA < eventIdB.
     * Значение: Сумма минимальных весов
     * Ключ: строка вида "eventIdA:eventIdB".
     */
    Map<String, Double> minWeightsPairSum = new ConcurrentHashMap<>();

    /**
     * Множество событий, с которыми взаимодействовал каждый пользователь.
     * для быстрой итерации по событиям пользователя при обновлении.
     * Ключ: userId.
     * Значение: Множество Id событий Set<Long>.
     */
    Map<Long, Set<Long>> eventsByUser = new ConcurrentHashMap<>();

    /**
     * Надеюсь, не перемудрила...
         * Блокировка для защиты операций, связанных с изменением {@link #eventsByUser}.
     * Так как HashSet не потокобезопасен, сделаем механизм для синхронизации
     * его изменений. ReadWriteLock позволяет нескольким читателям работать
     * одновременно, но требует блокировки для записи.
     */
    ReadWriteLock eventsByUserLock = new ReentrantReadWriteLock();
    Lock eventsByUserWriteLock = eventsByUserLock.writeLock();

    /**
     * Получает максимальный вес взаимодействия пользователя с событием.
     *
     * @return Максимальный вес или 0, если пользователь еще не взаимодействовал с этим событием.
     */
    public double getUserEventWeight(long userId, long eventId) {
        return userEventWeight.getOrDefault(getUserEventKey(userId, eventId), 0.0);
    }

    /**
     * Обновляет максимальный вес взаимодействия пользователя с событием.
     */
    public void updateUserEventWeight(long userId, long eventId, double weight) {
        String key = getUserEventKey(userId, eventId);

        userEventWeight.put(key, weight);
        log.trace("Обновлен вес пользователя {}:{}: {}", userId, eventId, weight);

        // Добавляем событие в множество пользователя, если его там еще нет.
        // для последующей итерации по всем событиям пользователя.
        // Требуется синхронизация, так как HashSet не потокобезопасен.
        eventsByUserWriteLock.lock();
        try {
            // Получаем или создаем множество событий для пользователя.
            eventsByUser.computeIfAbsent(userId, k -> new HashSet<>()).add(eventId);
            log.trace("Добавлено событие {} в множество пользователя {}. Размер текущего множества: {}",
                    eventId, userId, eventsByUser.get(userId).size());
        } finally {
            eventsByUserWriteLock.unlock();
        }
    }

    /**
     * Получает сумму квадратов весов для заданного события.
     *
     * @return Сумма квадратов весов или 0, если для события нет данных.
     */
    public double getEventWeightSum(long eventId) {
        return eventWeightSum.getOrDefault(eventId, 0.0);
    }

    /**
     * Обновляет сумму весов для события на дельту.
     * Прибавляет к текущему значению `deltaWeight`.
     *
     * @param deltaWeight Разница новых и старых весов (newWeight - oldWeight).
     *                           Если ключ отсутствует в карте, он добавляет новую пару ключ-значение.
     *                           Если ключ присутствует в карте, он обновляет существующее значение, используя функцию Double::sum
     *                           а именно лямбда-выражение (oldValue, newValue) -> oldValue + newValue.
     */
    public void updateEventWeightSum(long eventId, double deltaWeight) {
        // Используем merge для атомарного обновления значения в ConcurrentHashMap.
        // Если ключа нет, он будет создан со значением deltaWeight.
        // Если ключ есть, к нему будет применен лямбда-выражение: (oldValue, newValue) -> oldValue + newValue
        eventWeightSum.merge(eventId, deltaWeight, Double::sum);
        log.trace("Обновлена сумма квадратов для события {}. Дельта: {}. Новое значение: {}",
                eventId, deltaWeight, eventWeightSum.get(eventId));
    }

    /**
     * Получает сумму минимальных весов для пары событий.
     *
     * @return Сумма минимальных весов или 0, если для пары нет данных.
     */
    public double getMinWeightsPairSum(long eventId1, long eventId2) {
        return minWeightsPairSum.getOrDefault(getPairEventKey(eventId1, eventId2), 0.0);
    }

    /**
     * обновляет сумму минимальных весов для пары событий на дельту.
     * Прибавляет к текущему значению `deltaSMin`.
     *
     * @param deltaSMin Разница минимальных весов (min(newW, otherW) - min(oldW, otherW)).
     */
    public void updateMinWeightsPairSum(long eventId1, long eventId2, double deltaSMin) {
        String key = getPairEventKey(eventId1, eventId2);

        minWeightsPairSum.merge(key, deltaSMin, Double::sum);

        log.trace("Прошло обновление суммы мин. весов для пары {}:{}. Дельта: {}.",
                eventId1, eventId2, deltaSMin);
    }

    /**
     * Возвращает множество Id событий, с которыми взаимодействовал указанный пользователь.
     *
     * @return Неизменяемое множество Id событий. Если пользователь не найден, возвращает пустое множество.
     */
    public Set<Long> getEventsByUser(long userId) {
        Set<Long> userEvents = eventsByUser.get(userId);

        // Возвращаем копию, чтобы никто не поменял в другом месте

        if (userEvents == null) {
            return Set.of();
        }

        return Set.copyOf(userEvents);
    }

    /**
     * Формирует уникальный ключ для хранения веса пользователя для события.
     * @param userId  Id пользователя.
     * @param eventId Id события.
     * @return Строковый ключ вида "userId:eventId".
     */
    private String getUserEventKey(long userId, long eventId) {
        return userId + ":" + eventId;
    }

    /**
     * Формирует уникальный ключ для хранения суммы весов для пары событий.
     * Гарантирует, что ключ будет одинаковым независимо от порядка Id событий
     * ключ вида "minEventId:maxEventId".
     */
    private String getPairEventKey(long eventId1, long eventId2) {
        if (eventId1 < eventId2) {
            return eventId1 + ":" + eventId2;
        } else {
            return eventId2 + ":" + eventId1;
        }
    }
}
