package event.service.events.model;

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
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import event.service.category.model.Category;
import interaction.api.enums.EventState;
import event.service.location.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "event")
public class EventModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "annotation", length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(name = "confirmed_requests")
    @Min(0)
    Long confirmedRequests;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "description", length = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "initiator_id", nullable = false)
    Long initiatorId;

    @Column(name = "paid", nullable = false)
    Boolean paid;

    @Column(name = "participant_limit")
    Long participantLimit;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    EventState state;

    @Column(name = "title", nullable = false, length = 120)
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;
}
