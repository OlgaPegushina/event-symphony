package request.service.feign.client;

import interaction.api.feign.contract.UserContract;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient extends UserContract {
}
