package main.service.events.model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import main.service.category.model.Category;
import main.service.events.enums.EventState;
import main.service.location.Location;
import main.service.user.model.User;
import org.hibernate.annotations.Formula;

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

    @Size(max = 2000)
    @Column(name = "annotation", length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull(message = "Category не должна быть пустой")
    Category category;

    @Formula("(select count(*) from participation_request p " +
            " where p.event_id = id and p.status = 'CONFIRMED')")
    Long confirmedRequests;

    @Column(name = "created_on")
    @NotNull(message = "СreatedOn не должна быть пустой")
    LocalDateTime createdOn;

    @Size(max = 7000)
    @Column(name = "description", length = 7000)
    String description;

    @NotNull(message = "EventDate не должна быть пустой")
    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    @NotNull(message = "Initiator не должен быть пустым")
    User initiator;

    @NotNull(message = "Paid не должен быть пустым")
    @Column(name = "paid")
    Boolean paid;

    @Column(name = "participant_limit")
    Long participantLimit;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @NotNull(message = "RequestModeration не должна быть пустой")
    @Column(name = "request_moderation")
    Boolean requestModeration;

    @NotNull(message = "State не должен быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    EventState state;

    @Size(max = 120)
    @NotBlank(message = "Title cannot be blank")
    @Column(name = "title")
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @NotNull(message = "Location не должна быть пустой")
    Location location;
}
