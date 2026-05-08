package vn.nbh.notificationservice.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Configuration
@Slf4j
public class SystemFeignConfig {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            try {
                // Tự động sinh một JWT Token nội bộ sống trong 5 phút, có quyền ADMIN
                JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
                JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                        .subject("system-notification")
                        .issuer("nbh.vn")
                        .expirationTime(new Date(Instant.now().plus(5, ChronoUnit.MINUTES).toEpochMilli()))
                        .jwtID(UUID.randomUUID().toString())
                        .claim("scope", "ROLE_ADMIN") // Cấp quyền tối cao
                        .build();

                JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
                jwsObject.sign(new MACSigner(signerKey.getBytes()));

                // Nhét Token vào Header trước khi gọi API
                template.header("Authorization", "Bearer " + jwsObject.serialize());
            } catch (Exception e) {
                log.error("Lỗi khi tạo System Token: ", e);
            }
        };
    }
}