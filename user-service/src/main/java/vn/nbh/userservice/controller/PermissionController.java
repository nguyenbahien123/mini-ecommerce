package vn.nbh.userservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.nbh.userservice.dto.request.PermissionRequest;
import vn.nbh.userservice.dto.response.ApiResponse;
import vn.nbh.userservice.dto.response.PermissionPageResponse;
import vn.nbh.userservice.dto.response.PermissionResponse;
import vn.nbh.userservice.service.PermissionService;

import java.util.List;

@RestController
@RequestMapping("api/v1/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Permission API", description = "API cho việc quản lý quyền hạn (Permission)")
public class PermissionController {
    PermissionService permissionService;

    @Operation(summary = "Tạo quyền hạn mới", description = "Chỉ ADMIN mới có quyền tạo quyền hạn mới")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest permissionRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(permissionRequest))
                .build();
    }

    @Operation(summary = "Cập nhật quyền hạn", description = "Chỉ ADMIN mới có quyền cập nhật quyền hạn")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{permissionId}")
    ApiResponse<PermissionResponse> update(@PathVariable("permissionId") String permissionId
                                            ,@RequestBody PermissionRequest permissionRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.update(permissionId, permissionRequest))
                .build();
    }

    @Operation(summary = "Lấy danh sách tất cả quyền hạn", description = "Chỉ ADMIN mới có quyền xem danh sách tất cả quyền hạn")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @Operation(summary = "Xóa quyền hạn", description = "Chỉ ADMIN mới có quyền xóa quyền hạn")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{permissionId}")
    ApiResponse<Void> delete(@PathVariable String permissionId) {
        permissionService.delete(permissionId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Tìm kiếm và phân trang quyền hạn", description = "Chỉ ADMIN mới có quyền tìm kiếm và phân trang quyền hạn")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    ApiResponse<PermissionPageResponse> findAll(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size){
        return ApiResponse.<PermissionPageResponse>builder()
                .result(permissionService.findAll(keyword,sort,page,size))
                .build();
    }
}
