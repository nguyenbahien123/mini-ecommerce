package vn.nbh.userservice.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.nbh.userservice.repository.InvalidTokenRepository;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupSchedule {
    private final InvalidTokenRepository invalidTokenRepository;

    /**
     * Tự động chạy quét database để xóa token hết hạn.
     * cron = "0 0 1 * * ?" : Chạy vào lúc 1 giờ sáng mỗi ngày.
     * fixedRate = 3600000 : Hoặc chạy mỗi tiếng 1 lần (đơn vị milisec).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Bắt đầu dọn dẹp các token đã hết hạn trong Blacklist...");

        try {
            invalidTokenRepository.deleteAllByExpiryTimeBefore(new Date());
            log.info("Dọn dẹp token hoàn tất!");
        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp token hết hạn: ", e);
        }
    }
}
