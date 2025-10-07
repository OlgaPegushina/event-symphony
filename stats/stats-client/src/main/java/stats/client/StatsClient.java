package stats.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stats.dto.dto.EndpointHitDto;
import stats.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@Service
@SuppressWarnings("unused")
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseUrl = "http://stat-server:9090";
    }

    public ResponseEntity<EndpointHitDto> postHit(EndpointHitDto endpointHitDto) {
        String url = baseUrl + "/hit";
        return restTemplate.postForEntity(url, endpointHitDto, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.format(DATE_TIME_FORMATTER);
        String endStr = end.format(DATE_TIME_FORMATTER);

        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("/stats?start=").append(startStr)
                .append("&end=").append(endStr)
                .append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(uri);
            }
        }

        String url = urlBuilder.toString();
        ResponseEntity<ViewStatsDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                ViewStatsDto[].class
        );
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }
}