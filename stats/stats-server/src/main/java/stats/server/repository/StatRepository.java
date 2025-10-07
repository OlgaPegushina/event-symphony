package stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stats.dto.dto.ViewStatsDto;
import stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new stats.dto.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.ip)) " +
            "FROM EndpointHit AS eh " +
            "WHERE eh.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR eh.uri IN :uris) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) DESC")
    List<ViewStatsDto> getStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris);

    @Query("SELECT new stats.dto.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip)) " +
            "FROM EndpointHit eh " +
            "WHERE eh.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR eh.uri IN :uris) " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStatsDto> getUniqueStats(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}