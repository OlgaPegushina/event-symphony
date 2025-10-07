package main.service.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 250, message = "Имя должно быть меньше 250 символов")
    @Column(name = "name")
    String name;

    @NotBlank(message = "Email Имя не может быть пустым")
    @Email(message = "Email должен быть действительным")
    @Size(max = 254, message = "Email должен быть меньше 254 символов")
    @Column(name = "email")
    String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (user.id != null && id != null) {
            return id.equals(user.id);
        }
        return Objects.equals(name, user.name) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : Objects.hash(name, email);
    }
}