package analyzer.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "analyzer.weights")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeightProperties {
    Double view;
    Double register;
    Double like;
}
