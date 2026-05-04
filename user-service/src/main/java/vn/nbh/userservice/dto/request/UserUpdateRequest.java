package vn.nbh.userservice.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    private String username;
    private String password;
    private String address;
    private String phoneNumber;
    private String roles;
}
