package main.service.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryDto {

    Long id;
    @NotBlank(message = "Поле 'name' должно быть заполнено")
    @Size(min = 1, max = 50, message = "Размер поля 'name' должен быть в диапазоне от 1 до 50 символов")
    String name;
}