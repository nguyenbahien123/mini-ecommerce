package vn.nbh.notificationservice.dto.request;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BrevoEmailRequest {
    private Sender sender;
    private List<To> to;
    private String subject;
    private String htmlContent;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Sender {
        private String name;
        private String email;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class To {
        private String name;
        private String email;
    }
}