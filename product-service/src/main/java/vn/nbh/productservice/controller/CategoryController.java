package vn.nbh.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.nbh.productservice.dto.request.CategoryRequest;
import vn.nbh.productservice.dto.response.ApiResponse;
import vn.nbh.productservice.dto.response.CategoryResponse;
import vn.nbh.productservice.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .message("Tạo danh mục thành công")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .message("Cập nhật danh mục thành công")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .result("Đã xóa danh mục thành công")
                .build();
    }
}