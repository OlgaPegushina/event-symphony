package main.service.events.repository;

import main.service.events.model.EventModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventModel, Long> {
    boolean existsByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<EventModel> findAllByCategoryId(Long catId);

    Page<EventModel> findByInitiatorId(Long userId, PageRequest eventDate);

    Optional<EventModel> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("""
                SELECT e
                FROM EventModel AS e
                WHERE e.state = PUBLISHED
                AND (?1 IS NULL OR e.annotation ILIKE %?1% OR e.description ILIKE %?1%)
                AND (?2 IS NULL OR e.category.id IN ?2)
                AND (?3 IS NULL OR e.paid = ?3)
                AND (CAST(?4 AS timestamp) IS NULL AND e.eventDate >= CURRENT_TIMESTAMP OR e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL OR e.eventDate < ?5)
                AND (?6 = false OR e.participantLimit = 0 OR e.participantLimit < e.confirmedRequests)
            """)
    List<EventModel> findAllByFiltersPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable pageable);

    @Query("""
                SELECT e
                FROM EventModel AS e
                WHERE (?1 IS NULL OR e.initiator.id IN ?1)
                AND (?2 IS NULL OR e.state IN ?2)
                AND (?3 IS NULL OR e.category.id IN ?3)
                AND (CAST(?4 AS timestamp) IS NULL OR e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL OR e.eventDate < ?5)
            """)
    List<EventModel> findAllByFiltersAdmin(List<Long> users, List<String> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);
}