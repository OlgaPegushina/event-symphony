package interaction.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDto {
    Long id;

    @Size(min = 2, max = 250)
    @NotBlank(message = "Имя пользователя не может быть пустым")
    String name;

    @Size(min = 6, max = 254)
    @NotBlank(message = "Email пользователя не может быть пустым")
    @Email(message = "Email имеет неверный формат")
    String email;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }
}
