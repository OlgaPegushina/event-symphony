package aggregator.kafka.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "aggregator") // -- Связываем класс с префиксом в yaml
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatorProperties {
    Map<ActionTypeAvro, Double> weights;
}
