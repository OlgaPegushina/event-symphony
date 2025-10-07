package main.service.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.service.compilation.dto.CompilationDto;
import main.service.compilation.dto.CompilationsRequest;
import main.service.compilation.pagination.PaginationOffset;
import main.service.compilation.service.CompilationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable long compId) {
        log.info("Получаем подборку по id={}", compId);
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@ModelAttribute @Validated CompilationsRequest compilationsRequest,
                                                @ModelAttribute @Validated PaginationOffset paginationOffset) {
        log.info("Получаем все подборки");
        return compilationService.getCompilations(compilationsRequest, paginationOffset);
    }
}
