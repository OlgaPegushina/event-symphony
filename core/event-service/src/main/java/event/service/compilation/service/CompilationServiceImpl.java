package event.service.compilation.service;

import com.querydsl.core.types.Predicate;
import interaction.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import event.service.compilation.CompilationMapper;
import event.service.compilation.CompilationRepository;
import event.service.compilation.dto.NewCompilationDto;
import event.service.compilation.dto.CompilationDto;
import event.service.compilation.dto.CompilationUpdateDto;
import event.service.compilation.dto.CompilationsRequest;
import event.service.compilation.model.Compilation;
import event.service.compilation.model.QCompilation;
import event.service.compilation.pagination.PaginationOffset;
import event.service.events.model.EventModel;
import event.service.events.services.impls.PrivateServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final PrivateServiceImpl eventService;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        CompilationMapper.MapperContext context = new CompilationMapper.MapperContext(eventService);
        Compilation compilation = compilationMapper.toEntity(newCompilationDto, context);

        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<EventModel> events = new HashSet<>(eventService.findAllById(new ArrayList<>(newCompilationDto.getEvents())));
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new HashSet<>());
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Создаем подборку");
        return compilationMapper.toDto(savedCompilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Событие с id " + compId + " не найдено");
        }
        log.info("Удаляем подборку id={}", compId);
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, CompilationUpdateDto updateDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + compId + " не найдено"));
        if (updateDto.getTitle() != null) {
            compilation.setTitle(updateDto.getTitle());
        }
        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getEvents() != null) {
            Set<EventModel> events = new HashSet<>(eventService.findAllById(new ArrayList<>(updateDto.getEvents())));
            compilation.setEvents(events);
        }
        log.info("Обновляем подборку id={}", compId);
        return compilationMapper.toDto(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + compId + " не найдено"));
        return compilationMapper.toDto(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilations(CompilationsRequest request, PaginationOffset pagination) {
        QCompilation qCompilation = QCompilation.compilation;
        Predicate predicate = null;
        if (request.getPinned() != null) {
            predicate = qCompilation.pinned.eq(request.getPinned());
        }
        int from = (pagination.getFrom() != null) ? pagination.getFrom() : 0;
        int size = (pagination.getSize() != null) ? pagination.getSize() : 10;
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> page = (predicate != null)
                ? compilationRepository.findAll(predicate, pageable)
                : compilationRepository.findAll(pageable);
        return compilationMapper.toDtoList(page.getContent());
    }
}
