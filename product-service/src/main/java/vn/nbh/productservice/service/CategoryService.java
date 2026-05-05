package vn.nbh.productservice.service;

import vn.nbh.productservice.dto.request.CategoryRequest;
import vn.nbh.productservice.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> getAllCategories(); // Category thường ít dữ liệu nên trả về List thay vì PageDTO
}