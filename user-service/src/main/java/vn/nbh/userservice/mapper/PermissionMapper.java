package vn.nbh.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.nbh.userservice.dto.request.PermissionRequest;
import vn.nbh.userservice.dto.response.PermissionResponse;
import vn.nbh.userservice.entity.Permission;


@Mapper
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);

    PermissionResponse toPermissionResponse(Permission permission);

    void updatePermission(@MappingTarget Permission permission, PermissionRequest permissionRequest);
}
