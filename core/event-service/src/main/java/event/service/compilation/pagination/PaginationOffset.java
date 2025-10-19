package event.service.compilation.pagination;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Validated
@Getter
@Setter
public class PaginationOffset {
    @PositiveOrZero
    private Integer from;

    @Positive
    private Integer size;
}
