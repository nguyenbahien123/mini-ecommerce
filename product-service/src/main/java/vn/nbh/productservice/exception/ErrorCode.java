package vn.nbh.productservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(1001, "Unauthorized", HttpStatusCode.valueOf(401)),
    FORBIDDEN(1002, "Forbidden", HttpStatusCode.valueOf(403)),
    NOT_FOUND(1003, "Not Found", HttpStatusCode.valueOf(404)),
    INTERNAL_SERVER_ERROR(1004, "Internal Server Error", HttpStatusCode.valueOf(500)),
    INVALID_INPUT(1005, "Invalid Input", HttpStatusCode.valueOf(400)),
    PRODUCT_NOT_FOUND(1006, "Product Not Found", HttpStatusCode.valueOf(404)),
    PRODUCT_ALREADY_EXISTS(1007, "Product Already Exists", HttpStatusCode.valueOf(409)),
    INVALID_PRODUCT_DATA(1008, "Invalid Product Data", HttpStatusCode.valueOf(400)),
    DATABASE_ERROR(1009, "Database Error", HttpStatusCode.valueOf(500)),
    EXTERNAL_SERVICE_ERROR(1010, "External Service Error", HttpStatusCode.valueOf(502)),
    CATEGORY_NOT_FOUND(1011, "Category Not Found", HttpStatusCode.valueOf(404)),
    CATEGORY_ALREADY_EXISTS( 1012, "Category Already Exists", HttpStatusCode.valueOf(409))
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
