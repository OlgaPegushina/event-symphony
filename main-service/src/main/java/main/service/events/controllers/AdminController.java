package main.service.events.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.service.events.dto.EventFullDto;
import main.service.events.dto.UpdateEventAdminRequest;
import main.service.events.services.AdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static stats.dto.constant.Const.DATE_TIME_FORMAT;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminController {
    AdminService adminService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                    @PathVariable @Positive Long eventId) {
        log.info("Получен запрос на обновление события у админа");
        return adminService.updateEvent(updateEventAdminRequest, eventId);
    }

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Long> categories,

                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                        LocalDateTime rangeStart,

                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                        LocalDateTime rangeEnd,

                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поступил запрос на обновление события для админа");
        return adminService.getEventsWithAdminFilters(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}