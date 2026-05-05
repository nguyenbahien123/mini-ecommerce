package vn.nbh.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.nbh.userservice.dto.request.RoleRequest;
import vn.nbh.userservice.dto.response.ApiResponse;
import vn.nbh.userservice.dto.response.RolePageResponse;
import vn.nbh.userservice.dto.response.RoleResponse;
import vn.nbh.userservice.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Role API", description = "API cho việc quản lý vai trò (Role)")
public class RoleController {
    RoleService roleService;

    @Operation(summary = "Tạo vai trò mới", description = "Chỉ ADMIN mới có quyền tạo vai trò mới")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest roleRequest) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(roleRequest))
                .build();
    }

    @Operation(summary = "Cập nhật vai trò", description = "Chỉ ADMIN mới có quyền cập nhật vai trò")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @Operation(summary = "Cập nhật vai trò", description = "Chỉ ADMIN mới có quyền cập nhật vai trò")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{roleId}")
    ApiResponse<Void> delete(@PathVariable String roleId) {
        roleService.delete(roleId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Cập nhật vai trò", description = "Chỉ ADMIN mới có quyền cập nhật vai trò")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    ApiResponse<RolePageResponse> findAll(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size){
        return ApiResponse.<RolePageResponse>builder()
                .result(roleService.findAll(keyword,sort,page,size))
                .build();
    }
}
