package vn.nbh.userservice.controller;

import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.nbh.userservice.dto.request.AuthenticationRequest;
import vn.nbh.userservice.dto.request.IntrospectRequest;
import vn.nbh.userservice.dto.request.LogoutRequest;
import vn.nbh.userservice.dto.request.RefreshRequest;
import vn.nbh.userservice.dto.response.ApiResponse;
import vn.nbh.userservice.dto.response.AuthenticationResponse;
import vn.nbh.userservice.dto.response.IntrospectResponse;
import vn.nbh.userservice.service.AuthenticationService;

import java.text.ParseException;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Authentication API", description = "API cho việc xác thực, cấp token, kiểm tra token và đăng xuất")
public class AuthenticationController {

    AuthenticationService authenticationService;

    @Operation(summary = "Đăng nhập", description = "Cấp phát Access Token và Refresh Token khi cung cấp email/mật khẩu đúng")
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        var result = authenticationService.authenticate(authenticationRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @Operation(summary = "Kiểm tra Token", description = "Xác minh xem Access Token còn hiệu lực và có hợp lệ không")
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) {
        var result = authenticationService.introspect(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @Operation(summary = "Làm mới Token", description = "Dùng Refresh Token để lấy một Access Token mới")
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest refreshRequest)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(refreshRequest);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa Token hiện tại, đẩy Token vào Blacklist")
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException, JOSEException {
        authenticationService.logout(logoutRequest);
        return ApiResponse.<Void>builder().build();
    }

}
