package request.service.repository;

import request.service.model.ParticipationRequest;
import interaction.api.enums.RequestStatus;
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

    @Query("SELECT r.eventId, COUNT(r) FROM ParticipationRequest r WHERE r.eventId IN :eventIds AND r.status = :status GROUP BY r.eventId")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);
}
