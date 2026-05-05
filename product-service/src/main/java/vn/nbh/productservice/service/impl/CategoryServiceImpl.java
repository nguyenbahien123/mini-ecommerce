package vn.nbh.productservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vn.nbh.productservice.dto.request.CategoryRequest;
import vn.nbh.productservice.dto.response.CategoryResponse;
import vn.nbh.productservice.entity.Category;
import vn.nbh.productservice.exception.AppException;
import vn.nbh.productservice.exception.ErrorCode;
import vn.nbh.productservice.repository.CategoryRepository;
import vn.nbh.productservice.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Kiểm tra xem tên mới có bị trùng với một category khác đã tồn tại không
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        // Lưu ý: Nếu có Product đang liên kết với Category này, việc xóa có thể bị lỗi khóa ngoại (Foreign Key).
        // Trong thực tế, người ta thường set is_active = false thay vì xóa cứng.
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}