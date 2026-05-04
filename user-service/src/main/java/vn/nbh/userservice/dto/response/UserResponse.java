package vn.nbh.userservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private int id;
    private String username;
    private String email;
    private String address;
    private String phoneNumber;
    private RoleResponse roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
