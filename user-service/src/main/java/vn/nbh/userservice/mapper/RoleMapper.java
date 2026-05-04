package vn.nbh.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.nbh.userservice.dto.request.RoleRequest;
import vn.nbh.userservice.dto.response.RoleResponse;
import vn.nbh.userservice.entity.Role;

@Mapper
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);

    RoleResponse toRoleResponse(Role role);
}
