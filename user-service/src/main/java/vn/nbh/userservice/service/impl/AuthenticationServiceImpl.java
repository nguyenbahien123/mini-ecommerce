package vn.nbh.userservice.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.nbh.userservice.dto.request.AuthenticationRequest;
import vn.nbh.userservice.dto.request.IntrospectRequest;
import vn.nbh.userservice.dto.request.LogoutRequest;
import vn.nbh.userservice.dto.request.RefreshRequest;
import vn.nbh.userservice.dto.response.AuthenticationResponse;
import vn.nbh.userservice.dto.response.IntrospectResponse;
import vn.nbh.userservice.entity.InvalidatedToken;
import vn.nbh.userservice.entity.User;
import vn.nbh.userservice.exception.AppException;
import vn.nbh.userservice.exception.ErrorCode;
import vn.nbh.userservice.repository.InvalidTokenRepository;
import vn.nbh.userservice.repository.UserRepository;
import vn.nbh.userservice.service.AuthenticationService;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvalidTokenRepository invalidTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY; // Chìa khóa bí mật dùng để ký và xác thực tính toàn vẹn của Token

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        // Kiểm tra sự tồn tại của người dùng qua Email
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // So khớp mật khẩu đã mã hóa trong DB với mật khẩu người dùng nhập vào
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPasswordHash());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Tạo mới Access Token (dùng để gọi API) và Refresh Token (dùng để cấp lại Access Token)
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        try {
            // Xác thực tính hợp lệ của cả 2 token trước khi thực hiện hủy (vô hiệu hóa)
            var signedAccessToken = verifyAccessToken(logoutRequest.getAccessToken());
            var signedRefreshToken = verifyRefreshToken(logoutRequest.getRefreshToken());

            // Lấy JTI (JWT ID) - mã định danh duy nhất của mỗi token để lưu vào danh sách đen (Blacklist)
            String jitAccessToken = signedAccessToken.getJWTClaimsSet().getJWTID();
            String jitRefreshToken = signedRefreshToken.getJWTClaimsSet().getJWTID();

            // Lưu thông tin token bị hủy vào DB để ngăn chặn việc sử dụng lại cho đến khi chúng hết hạn tự nhiên
            invalidTokenRepository.save(InvalidatedToken.builder().id(jitAccessToken).expiryTime(signedAccessToken.getJWTClaimsSet().getExpirationTime()).build());
            invalidTokenRepository.save(InvalidatedToken.builder().id(jitRefreshToken).expiryTime(signedRefreshToken.getJWTClaimsSet().getExpirationTime()).build());

        } catch (Exception e) {
            log.error("Cannot logout user", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest refreshRequest) throws ParseException, JOSEException {
        // 1. Kiểm tra Refresh Token gửi lên có hợp lệ không (chữ ký, hết hạn, blacklist)
        var signedJWT = verifyRefreshToken(refreshRequest.getRefreshToken());

        // 2. Kiểm tra nếu Refresh Token sắp hết hạn (ví dụ còn dưới 1) thì bắt đăng nhập lại hoặc xử lý riêng
        if(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()).after(signedJWT.getJWTClaimsSet().getExpirationTime())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3. Lấy thông tin user từ subject (email) lưu trong token để tạo Access Token mới
        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String accessToken = generateAccessToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshRequest.getRefreshToken()) // Giữ nguyên Refresh Token cũ hoặc có thể xoay vòng (rotate) nếu muốn
                .authenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        // Phương thức dùng để kiểm tra nhanh một token còn hiệu lực hay không (thường dùng cho các Service khác check chéo)
        String accessToken = introspectRequest.getAccessToken();
        boolean isValid = true;
        try {
            verifyAccessToken(accessToken);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }

    /**
     * Xác thực Access Token
     */
    private SignedJWT verifyAccessToken(String accessToken) throws JOSEException, ParseException {
        // Tạo bộ xác thực bằng khóa bí mật (thuật toán đối xứng MAC)
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // Giải mã chuỗi Token thành đối tượng SignedJWT để đọc dữ liệu bên trong
        SignedJWT signedJWT = SignedJWT.parse(accessToken);

        // Kiểm tra xem chữ ký (Signature) có đúng với khóa SIGNER_KEY không
        var verified = signedJWT.verify(verifier);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Kiểm tra tính chất: Access Token bắt buộc phải có scope (quyền hạn) dài hơn 6 ký tự
        if(signedJWT.getJWTClaimsSet().getClaim("scope").toString().length() < 6  ){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Nếu chữ ký sai HOẶC token đã quá thời gian hết hạn
        if(!verified && expirationTime.after(new Date())){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Kiểm tra ID của token này có nằm trong bảng InvalidatedToken (đã Logout) hay không
        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    /**
     * Xác thực Refresh Token
     */
    private SignedJWT verifyRefreshToken(String refreshToken) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(refreshToken);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        // Refresh Token thì scope thường để trống (length <= 6) để phân biệt với Access Token
        if(signedJWT.getJWTClaimsSet().getClaim("scope").toString().length() > 6  ){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if(!verified && expiryTime.after(new Date())){
            throw  new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    /**
     * Tạo Access Token (Thời hạn ngắn, chứa quyền hạn người dùng)
     */
    private String generateAccessToken(User user) {
        // Header: Khai báo thuật toán mã hóa HS512
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Claims: Nội dung payload (thông tin người dùng và metadata của token)
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail()) // Định danh người dùng
                .issueTime(new Date()) // Thời điểm phát hành
                .issuer("nbh.vn") // Đơn vị phát hành
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())) // Hết hạn sau 24h
                .jwtID(UUID.randomUUID().toString()) // Tạo ID ngẫu nhiên cho token để quản lý Logout
                .claim("scope", buildScope(user)) // Gán quyền (Roles & Permissions) vào token
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try{
            // Thực hiện ký bằng thuật toán MAC (khóa đối xứng)
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize(); // Trả về chuỗi JWT hoàn chỉnh (Header.Payload.Signature)
        } catch (JOSEException e) {
            log.error("Cannot create access token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tạo Refresh Token (Thời hạn dài, dùng để lấy Access Token mới mà không cần login lại)
     */
    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(new Date())
                .issuer("nbh.com")
                .expirationTime(new Date(Instant.now().plus(14, ChronoUnit.DAYS).toEpochMilli())) // Hết hạn sau 14 ngày
                .jwtID(UUID.randomUUID().toString())
                .claim("scope","") // Refresh Token không nên chứa quyền hạn (Security Best Practice)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (Exception e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gộp các Role và Permission thành một chuỗi phân cách bởi dấu cách (ví dụ: "ROLE_ADMIN USER_READ")
     */
    private Object buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(user.getRoles() != null){
            stringJoiner.add("ROLE_" + user.getRoles().getName());
            if(!user.getRoles().getPermissions().isEmpty()){
                user.getRoles().getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }
        return stringJoiner.toString();
    }
}