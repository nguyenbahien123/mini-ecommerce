package vn.nbh.orderservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(1001, "Unauthorized", HttpStatusCode.valueOf(401)),
    FORBIDDEN(1002, "Forbidden", HttpStatusCode.valueOf(403)),
    NOT_FOUND(1003, "Not Found", HttpStatusCode.valueOf(404)),
    INTERNAL_SERVER_ERROR(1004, "Internal Server Error", HttpStatusCode.valueOf(500)),
    INVALID_INPUT(1005, "Invalid Input", HttpStatusCode.valueOf(400)),
    PAYMENT_FAILED(1006, "Payment Failed", HttpStatusCode.valueOf(402)),
    OUT_OF_STOCK(1007, "Out of Stock", HttpStatusCode.valueOf(409)) ,
    ORDER_ALREADY_CANCELLED(1008, "Order Already Cancelled", HttpStatusCode.valueOf(409)),
    ORDER_NOT_FOUND(1009, "Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1010, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(1011, "Sản phẩm không đủ số lượng tồn kho", HttpStatus.BAD_REQUEST),
    SERVICE_UNAVAILABLE(1012, "Hệ thống đang quá tải, vui lòng thử lại", HttpStatusCode.valueOf(503))
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
