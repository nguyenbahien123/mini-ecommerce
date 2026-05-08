package vn.nbh.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.nbh.notificationservice.config.SystemFeignConfig;
import vn.nbh.notificationservice.dto.response.ApiResponse;
import vn.nbh.notificationservice.dto.response.UserResponse;

@FeignClient(name = "user-service", configuration = SystemFeignConfig.class)
public interface UserClient {
    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Integer id);
}
