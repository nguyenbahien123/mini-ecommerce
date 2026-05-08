package vn.nbh.paymentservice.exception;

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
    INSUFFICIENT_FUNDS(1007, "Insufficient Funds", HttpStatusCode.valueOf(402)),
    INVALID_PAYMENT_METHOD(1008, "Invalid Payment Method", HttpStatusCode.valueOf(400))
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
