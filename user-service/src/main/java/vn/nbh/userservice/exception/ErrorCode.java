package vn.nbh.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(1001, "Unauthorized", HttpStatusCode.valueOf(401)),
    FORBIDDEN(1002, "Forbidden", HttpStatusCode.valueOf(403)),
    NOT_FOUND(1003, "Not Found", HttpStatusCode.valueOf(404)),
    INTERNAL_SERVER_ERROR(1004, "Internal Server Error", HttpStatusCode.valueOf(500)),
    INVALID_INPUT(1005, "Invalid Input", HttpStatusCode.valueOf(400)),
    USER_NOT_FOUND(1006, "User Not Found", HttpStatusCode.valueOf(404)),
    USER_ALREADY_EXISTS(1007, "User Already Exists", HttpStatusCode.valueOf(409)),
    INVALID_USER_DATA(1008, "Invalid User Data", HttpStatusCode.valueOf(400)),
    DATABASE_ERROR(1009, "Database Error", HttpStatusCode.valueOf(500)),
    EXTERNAL_SERVICE_ERROR(1010, "External Service Error", HttpStatusCode.valueOf(502))
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
