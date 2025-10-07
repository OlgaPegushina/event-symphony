package stats.client;

import org.springframework.http.ResponseEntity;
import stats.dto.dto.EndpointHitDto;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public interface ClientBase {

    ResponseEntity<EndpointHitDto> postHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
