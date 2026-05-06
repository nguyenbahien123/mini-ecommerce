package vn.nbh.productservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vn.nbh.productservice.dto.request.ProductRequest;
import vn.nbh.productservice.dto.response.PageDTO;
import vn.nbh.productservice.dto.response.ProductResponse;
import vn.nbh.productservice.entity.Category;
import vn.nbh.productservice.entity.Product;
import vn.nbh.productservice.event.OrderCreatedEvent;
import vn.nbh.productservice.exception.AppException;
import vn.nbh.productservice.exception.ErrorCode;
import vn.nbh.productservice.repository.CategoryRepository;
import vn.nbh.productservice.repository.ProductRepository;
import vn.nbh.productservice.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Override
    @Transactional // Thêm Transactional cho create
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest productRequest) {
        if(productRepository.existsByName(productRequest.getName())){
            throw new AppException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .imageUrl(productRequest.getImageUrl())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .category(category)
                .build();


        product = productRepository.save(product);
        return mapToResponse(product);

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);


        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return mapToResponse(product);
    }

    @Override
    public PageDTO getProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size); // Frontend thường gửi page=1
        Page<Product> productPage = productRepository.findAll(pageRequest);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageDTO(
                productPage.getSize(),
                productPage.getNumber() + 1,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.isFirst(),
                productPage.isLast(),
                content
        );
    }

    @Override
    @Transactional // BẮT BUỘC PHẢI CÓ ĐỂ KÍCH HOẠT OPTIMISTIC LOCKING VÀ ROLLBACK
    public void deductInventory(List<OrderCreatedEvent.OrderItemEvent> items) {
        log.info("Bắt đầu xử lý trừ kho...");

        for (OrderCreatedEvent.OrderItemEvent item : items) {
            // 1. Tìm sản phẩm
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            // 2. Kiểm tra tồn kho
            if (product.getStockQuantity() < item.getQuantity()) {
                log.error("Sản phẩm ID {} không đủ hàng. Còn: {}, Yêu cầu: {}",
                        product.getId(), product.getStockQuantity(), item.getQuantity());
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            // 3. Trừ đi số lượng
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());

            // 4. Lưu lại
            // NẾU có 2 luồng cùng update 1 lúc, dòng code này của luồng chạy chậm hơn sẽ
            // ném ra lỗi ObjectOptimisticLockingFailureException.
            // Ngay lập tức Transaction bị Rollback -> Kafka Consumer bên ngoài sẽ hứng lỗi và tự Retry!
            productRepository.save(product);
        }

        log.info("Trừ kho hoàn tất tất cả sản phẩm!");
    }

    private ProductResponse mapToResponse(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
