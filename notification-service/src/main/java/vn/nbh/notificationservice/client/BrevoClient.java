package vn.nbh.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.nbh.notificationservice.dto.request.BrevoEmailRequest;

@FeignClient(name = "brevo-client", url = "https://api.brevo.com/v3")
public interface BrevoClient {

    @PostMapping("/smtp/email")
    void sendEmail(
            @RequestHeader("api-key") String apiKey,
            @RequestBody BrevoEmailRequest request
    );
}