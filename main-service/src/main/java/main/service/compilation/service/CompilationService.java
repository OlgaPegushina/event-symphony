package main.service.compilation.service;


import main.service.compilation.dto.NewCompilationDto;
import main.service.compilation.dto.CompilationDto;
import main.service.compilation.dto.CompilationUpdateDto;
import main.service.compilation.dto.CompilationsRequest;
import main.service.compilation.pagination.PaginationOffset;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, CompilationUpdateDto updateDto);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(CompilationsRequest compilationsRequest, PaginationOffset paginationOffset);
}
