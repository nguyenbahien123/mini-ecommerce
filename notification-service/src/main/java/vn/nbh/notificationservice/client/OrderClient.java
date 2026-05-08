package vn.nbh.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.nbh.notificationservice.config.SystemFeignConfig;
import vn.nbh.notificationservice.dto.response.ApiResponse;
import vn.nbh.notificationservice.dto.response.OrderResponse;

@FeignClient(name = "order-client", configuration = SystemFeignConfig.class)
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    ApiResponse<OrderResponse> getOrderById(@PathVariable("id") Long id);

}
