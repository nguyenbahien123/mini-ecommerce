package vn.nbh.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import vn.nbh.userservice.dto.request.IntrospectRequest;
import vn.nbh.userservice.service.impl.AuthenticationServiceImpl;

import javax.crypto.spec.SecretKeySpec;

/**
 * CustomJwtDecoder: Lớp tùy chỉnh cách Spring Security giải mã và xác thực Token.
 * Thay vì chỉ kiểm tra hết hạn, lớp này còn kiểm tra Token có bị vô hiệu hóa (Logout) hay không.
 */
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey; // Khóa bí mật dùng để xác thực chữ ký JWT

    private final AuthenticationServiceImpl authenticationServiceImpl;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String accessToken) throws JwtException {
        // 1. Kiểm tra tính hợp lệ nâng cao (Introspect)
        // Gọi xuống AuthenticationService để check xem token có nằm trong Blacklist (đã logout) không
        var response = authenticationServiceImpl.introspect(
                IntrospectRequest.builder().accessToken(accessToken).build()
        );

        // Nếu Service trả về không hợp lệ (sai signature, hết hạn, hoặc đã logout), chặn ngay lập tức
        if(!response.isValid()){
            throw new JwtException("Token không hợp lệ hoặc đã đăng xuất");
        }

        // 2. Khởi tạo NimbusJwtDecoder (chỉ khởi tạo một lần duy nhất - Singleton pattern)
        if(nimbusJwtDecoder == null){
            // Xác định thuật toán HS512 và khóa bí mật để chuẩn bị giải mã
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");

            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        // 3. Thực hiện giải mã tiêu chuẩn
        // Sau khi qua bước check "sống/chết" ở trên, tiến hành parse Token thành đối tượng Jwt của Spring
        return nimbusJwtDecoder.decode(accessToken);
    }
}