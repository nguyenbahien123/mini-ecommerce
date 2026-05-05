package vn.nbh.userservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.nbh.userservice.dto.request.UserCreationRequest;
import vn.nbh.userservice.dto.request.UserUpdateRequest;
import vn.nbh.userservice.dto.response.ApiResponse;
import vn.nbh.userservice.dto.response.UserPageResponse;
import vn.nbh.userservice.dto.response.UserResponse;
import vn.nbh.userservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User API", description = "API cho việc quản lý người dùng (User)")
public class UserController {
    UserService userService;

    @Operation(summary = "Tạo người dùng mới", description = "Tạo một người dùng mới với thông tin được cung cấp. Chỉ ADMIN mới có quyền tạo người dùng.")
    @PostMapping("/add")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @Operation(summary = "Lấy danh sách tất cả người dùng", description = "Trả về danh sách tất cả người dùng trong hệ thống. Chỉ ADMIN mới có quyền xem danh sách người dùng.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();
    }

    @Operation(summary = "Tìm kiếm và phân trang người dùng", description = "Tìm kiếm người dùng theo từ khóa, sắp xếp và phân trang kết quả. Chỉ ADMIN mới có quyền thực hiện tìm kiếm.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    ApiResponse<UserPageResponse> findAll(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size){
        return ApiResponse.<UserPageResponse>builder()
                .result(userService.findAll(keyword,sort,page,size))
                .build();
    }


    @Operation(summary = "Cập nhật thông tin người dùng", description = "Cập nhật thông tin của một người dùng cụ thể. Chỉ ADMIN mới có quyền cập nhật người dùng.")
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable Integer userId, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @Operation(summary = "Xóa người dùng", description = "Xóa một người dùng cụ thể khỏi hệ thống. Chỉ ADMIN mới có quyền xóa người dùng.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable Integer userId){
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .result("User has been deleted")
                .build();
    }
}
