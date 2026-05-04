package vn.nbh.userservice.service;

import vn.nbh.userservice.dto.request.PermissionRequest;
import vn.nbh.userservice.dto.response.PermissionPageResponse;
import vn.nbh.userservice.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest permissionRequest);
    PermissionResponse update(String permissionId, PermissionRequest permissionRequest);
    List<PermissionResponse> getAll();
    void delete(String permissionId);
    PermissionPageResponse findAll(String keyword, String sort, int page, int size);
}
