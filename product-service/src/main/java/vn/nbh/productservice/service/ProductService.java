package vn.nbh.productservice.service;

import vn.nbh.productservice.dto.request.ProductRequest;
import vn.nbh.productservice.dto.response.PageDTO;
import vn.nbh.productservice.dto.response.ProductResponse;
import vn.nbh.productservice.event.OrderCanceledEvent;
import vn.nbh.productservice.event.OrderCreatedEvent;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    void deleteProduct(Long id);
    PageDTO getProducts(int page, int size);
    void deductInventory(List<OrderCreatedEvent.OrderItemEvent> items);
    void restoreInventory(List<OrderCanceledEvent.OrderItemEvent> items);
}
