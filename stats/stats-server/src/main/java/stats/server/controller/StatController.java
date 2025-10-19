package stats.server.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import stats.dto.dto.EndpointHitDto;
import stats.dto.dto.ViewStatsDto;
import stats.server.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class StatController {
    StatService statService;

    /**
     * Если к эндпоинту был запрос, то сохраняем
     *
     * @param endpointHitDto с данными запроса
     * @return EndpointHitDto
     */
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveHit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Controller: Запрос на сохранение нового хита.");
        log.debug("Сохраняемый хит: {}", endpointHitDto);
        return statService.saveHit(endpointHitDto);
    }

    /**
     * Получение статистики по посещениям.
     *
     * @param start  Дата и время начала диапазона, за который нужно выгрузить статистику ("yyyy-MM-dd HH:mm:ss")
     * @param end    Дата и время конца диапазона, за который нужно выгрузить статистику ("yyyy-MM-dd HH:mm:ss")
     * @param uris   Список uri для которых нужно выгрузить статистику
     * @param unique Нужно ли учитывать только уникальные посещения по ip (default: false)
     * @return List<StatDto> со статистикой
     */
    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(@RequestParam(name = "start")
                                       @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                       LocalDateTime start,
                                       @RequestParam(name = "end")
                                       @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                       LocalDateTime end,
                                       @RequestParam(name = "uris", required = false) List<String> uris,
                                       @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {

        log.info("Controller: Запрос на получение статистики получен.");
        log.debug("Параметры запроса: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        return statService.getStats(start, end, (uris == null || uris.isEmpty() ? null : uris), unique);
    }
}