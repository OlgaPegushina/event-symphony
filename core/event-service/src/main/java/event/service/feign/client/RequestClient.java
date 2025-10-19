package event.service.feign.client;

import interaction.api.feign.contract.RequestContract;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="request-service")
public interface RequestClient extends RequestContract {
}
