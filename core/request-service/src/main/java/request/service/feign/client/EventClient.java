package request.service.feign.client;

import interaction.api.feign.contract.EventContract;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventContract {
}
