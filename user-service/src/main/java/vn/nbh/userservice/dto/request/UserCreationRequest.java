package vn.nbh.userservice.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreationRequest {
    private String username;
    private String password;
    private String email;
    private String address;
    private String phoneNumber;
}
