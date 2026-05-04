package vn.nbh.userservice.service;

import vn.nbh.userservice.dto.request.RoleRequest;
import vn.nbh.userservice.dto.response.RolePageResponse;
import vn.nbh.userservice.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest roleRequest);
    List<RoleResponse> getAll();
    void delete(String roleId);
    RolePageResponse findAll(String keyword, String sort, int page, int size);
}
