package vn.nbh.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    // Gán trực tiếp tên biến Enum ErrorCode vào message để GlobalExceptionHandler map tự động
    @NotBlank(message = "INVALID_PRODUCT_DATA")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "INVALID_PRODUCT_DATA")
    @Min(value = 0, message = "INVALID_PRODUCT_DATA")
    private BigDecimal price;

    @NotNull(message = "INVALID_PRODUCT_DATA")
    @Min(value = 0, message = "INVALID_PRODUCT_DATA")
    private Integer stockQuantity;

    @NotNull(message = "INVALID_PRODUCT_DATA")
    private Long categoryId;

}
