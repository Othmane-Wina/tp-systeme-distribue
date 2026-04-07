package com.demo.restapi.service;

import com.demo.restapi.dto.ProductFullDTO;
import com.demo.restapi.dto.ProductPreviewDTO;
import com.demo.restapi.entity.Category;
import com.demo.restapi.entity.Product;
import com.demo.restapi.entity.Seller;
import com.demo.restapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.demo.restapi.mapper.DTOMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.demo.restapi.repository.CategoryRepository;
import com.demo.restapi.repository.ProductRepository;
import com.demo.restapi.repository.SellerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final DTOMapper mapper;

    // Requirement: Get list sorted by createdAt
    public List<ProductPreviewDTO> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toProductPreview)
                .collect(Collectors.toList());
    }

    public ProductFullDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return mapper.toProductFull(product);
    }

    public Page<ProductPreviewDTO> searchProducts(String q, String category, Double minPrice, Pageable pageable) {
        // 1. Initialize with a conjunction (this is an 'always true' predicate)
        // This ensures 'spec' is never null.
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.conjunction());

        // 2. Add search keyword filter
        if (q != null && !q.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + q.toLowerCase() + "%")
                    )
            );
        }

        // 3. Add category filter
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("name"), category));
        }

        // 4. Add price filter
        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        // 5. Execute. Now 'spec' is guaranteed to be an object.
        return productRepository.findAll(spec, pageable)
                .map(mapper::toProductPreview);
    }

    public ProductFullDTO createProduct(ProductFullDTO dto) {
        // 1. Use Mapper to get the basic Entity
        Product product = mapper.toProductEntity(dto);

        // 2. Service handles "Relationship Linking" (Business Logic)
        Category category = categoryRepository.findByName(dto.getCategory().getName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Seller seller = sellerRepository.findById(dto.getSeller().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        product.setCategory(category);
        product.setSeller(seller);

        // 3. Save and map back to DTO
        return mapper.toProductFull(productRepository.save(product));
    }

    // Requirement: Update (price & stock allowed, seller forbidden)
    public ProductFullDTO updateProduct(String id, ProductFullDTO updateDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        existing.setPrice(updateDTO.getPrice());
        existing.setStock(updateDTO.getStock());
        // Note: We ignore any seller info in the updateDTO to satisfy the "seller forbidden" rule.

        return mapper.toProductFull(productRepository.save(existing));
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id))
            throw new ResourceNotFoundException(id);
        productRepository.deleteById(id);
    }
}
