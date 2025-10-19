package event.service.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "location")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Широта не может быть пустой")
    @DecimalMin(value = "-90.0", message = "Широта должна быть >= -90")
    @DecimalMax(value = "90.0", message = "Широта должна быть <= 90")
    @Column(name = "lat")
    Double lat;

    @NotNull(message = "Долгота не может быть пустой")
    @DecimalMin(value = "-180.0", message = "Долгота должна быть >= -180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть <= 180")
    @Column(name = "lon")
    Double lon;
}
