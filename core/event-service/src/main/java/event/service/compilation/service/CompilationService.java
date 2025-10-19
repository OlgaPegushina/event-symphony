package event.service.compilation.service;


import event.service.compilation.dto.NewCompilationDto;
import event.service.compilation.dto.CompilationDto;
import event.service.compilation.dto.CompilationUpdateDto;
import event.service.compilation.dto.CompilationsRequest;
import event.service.compilation.pagination.PaginationOffset;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, CompilationUpdateDto updateDto);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(CompilationsRequest compilationsRequest, PaginationOffset paginationOffset);
}
