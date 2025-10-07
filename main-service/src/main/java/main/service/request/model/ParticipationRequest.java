package main.service.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import main.service.events.model.EventModel;
import main.service.user.model.User;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "participation_request")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreatedDate
    @Column(name = "created", updatable = false)
    @NotNull(message = "Дата создания не может быть пустой")
    LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @NotNull(message = "Event не может быть пустым")
    EventModel event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    @NotNull(message = "Requester не может быть пустым")
    User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull(message = "Статус не может быть пустым")
    RequestStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipationRequest request = (ParticipationRequest) o;
        if (request.id != null && id != null) {
            return id.equals(request.id);
        }
        return Objects.equals(created, request.created) &&
                Objects.equals(event, request.event) &&
                Objects.equals(requester, request.requester) &&
                status == request.status;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : Objects.hash(created, event, requester, status);
    }
}
