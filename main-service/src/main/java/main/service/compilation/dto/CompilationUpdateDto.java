package main.service.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationUpdateDto {
    private Set<Long> events;
    private Boolean pinned;
    @Length(min = 1, max = 50)
    private String title;
}
