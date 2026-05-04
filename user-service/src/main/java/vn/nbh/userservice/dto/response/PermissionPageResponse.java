package vn.nbh.userservice.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PermissionPageResponse extends PageResponseAbstract {
    private List<PermissionResponse> permissions;
}
