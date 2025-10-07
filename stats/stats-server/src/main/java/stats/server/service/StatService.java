package stats.server.service;

import stats.dto.dto.EndpointHitDto;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    EndpointHitDto saveHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, Boolean unique);
}