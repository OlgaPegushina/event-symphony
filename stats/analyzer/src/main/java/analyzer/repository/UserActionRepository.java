package analyzer.repository;

import analyzer.model.UserAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    // -- получает ID всех событий, с которыми взаимодействовал пользователь.
    @Query("SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") Long userId);

    // -- Получает ID N последних событий, с которыми взаимодействовал пользователь.
    @Query("SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId")
    List<Long> findRecentEventIdsByUserId(Long userId, Pageable pageable);

    /**
     * Возвращает веса действий пользователя для указанных событий.
     * Использует default-метод для преобразования в Map.
     */
    default Map<Long, Double> findWeightsByUserIdAndEventIds(Long userId, Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return findActionWeights(userId, eventIds).stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Double) obj[1]));
    }

    @Query("SELECT ua.eventId, ua.actionWeight FROM UserAction ua WHERE ua.userId = :userId AND ua.eventId IN :eventIds")
    List<Object[]> findActionWeights(@Param("userId") Long userId, @Param("eventIds") Set<Long> eventIds);

    // -- Рассчитывает сумму весов для списка событий. Суммирует все actionWeight для каждого eventId.
    default Map<Long, Double> getAggregatedWeightsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return getSumOfWeights(eventIds).stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Double) obj[1]));
    }

    @Query("""
        SELECT ua.eventId, SUM(ua.actionWeight)
        FROM UserAction ua
        WHERE ua.eventId IN :eventIds
        GROUP BY ua.eventId
        """)
    List<Object[]> getSumOfWeights(@Param("eventIds") List<Long> eventIds);

    Optional<UserAction> findByUserIdAndEventId(long userId, long eventId);
}