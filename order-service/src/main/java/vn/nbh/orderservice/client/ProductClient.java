package vn.nbh.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.nbh.orderservice.config.FeignConfig;
import vn.nbh.orderservice.dto.response.ApiResponse;
import vn.nbh.orderservice.dto.response.ProductResponse;

// Gọi sang product-service kèm cấu hình nhét JWT Token vào Header
@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);
}