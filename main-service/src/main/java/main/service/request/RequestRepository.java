package main.service.request;

import main.service.request.model.ParticipationRequest;
import main.service.request.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatusEquals(Long eventId, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    long countByIdInAndEventId(List<Long> requestIds, Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    @Query("SELECT r.event.id, COUNT(r) FROM ParticipationRequest r WHERE r.event.id IN :eventIds AND r.status = :status GROUP BY r.event.id")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);
}
