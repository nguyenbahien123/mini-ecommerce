package vn.nbh.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    BLOG_NOT_EXISTED(1009, "Blog not existed ", HttpStatus.NOT_FOUND),
    BRAND_NOT_EXISTED(1010, "Brand not existed ", HttpStatus.NOT_FOUND),
    COLOR_NOT_EXISTED(1011, "Color not existed ", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXISTED(1012, "Category not existed ", HttpStatus.NOT_FOUND),
    SIZE_NOT_EXISTED(1012, "Size not existed ", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_EXISTED(1012, "Permission not existed ", HttpStatus.NOT_FOUND),
    ORDER_STATUS_NOT_EXISTED(1012, "Order status not existed ", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1012, "Role not existed ", HttpStatus.NOT_FOUND),
    SHOE_NOT_EXISTED(1013, "Shoe not existed ", HttpStatus.NOT_FOUND),
    SHOE_VARIANT_NOT_EXISTED(1014, "Shoe variant not existed ", HttpStatus.NOT_FOUND),
    IMAGE_NOT_EXISTED(1015, "Image not existed ", HttpStatus.NOT_FOUND),
    ORDER_NOT_EXISTED(1016, "Order not existed ", HttpStatus.NOT_FOUND),
    SLIDER_NOT_EXISTED(1017, "Slider not existed ", HttpStatus.NOT_FOUND),
    INVALID_TOKEN(1018, "Invalid token ", HttpStatus.BAD_REQUEST),
    INVALID_FILE(1018, "Invalid file ", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}

