package stats.server.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "endpoint_hit")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "app")
    String app;

    @Column(name = "uri")
    String uri;

    @Column(name = "ip")
    String ip;

    @Column(name = "timestamp")
    LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointHit hit = (EndpointHit) o;
        if (hit.id != null && id != null) {
            return id.equals(hit.id);
        }
        return Objects.equals(app, hit.app) &&
                Objects.equals(uri, hit.uri) &&
                Objects.equals(ip, hit.ip) &&
                Objects.equals(timestamp, hit.timestamp);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(app, uri, ip, timestamp);
    }
}