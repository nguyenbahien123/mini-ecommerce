package vn.nbh.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.nbh.productservice.dto.request.CategoryRequest;
import vn.nbh.productservice.dto.response.ApiResponse;
import vn.nbh.productservice.dto.response.CategoryResponse;
import vn.nbh.productservice.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "API quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Tạo danh mục mới", description = "Tạo một danh mục sản phẩm mới với thông tin được cung cấp")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .message("Tạo danh mục thành công")
                .build();
    }

    @Operation(summary = "Cập nhật danh mục", description = "Cập nhật thông tin của một danh mục sản phẩm đã tồn tại bằng ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .message("Cập nhật danh mục thành công")
                .build();
    }

    @Operation(summary = "Lấy thông tin danh mục", description = "Lấy thông tin chi tiết của một danh mục sản phẩm bằng ID")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(id))
                .build();
    }

    @Operation(summary = "Lấy danh sách danh mục", description = "Lấy danh sách tất cả các danh mục sản phẩm có trong hệ thống")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @Operation(summary = "Xóa danh mục", description = "Xóa một danh mục sản phẩm khỏi hệ thống bằng ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .result("Đã xóa danh mục thành công")
                .build();
    }
}