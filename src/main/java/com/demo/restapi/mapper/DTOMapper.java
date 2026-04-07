package com.demo.restapi.mapper;

import com.demo.restapi.dto.*;
import com.demo.restapi.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DTOMapper {

    // --- TO DTO (Entity -> DTO) ---

    // SELLER MAPPINGS
    public SellerPreviewDTO toSellerPreview(Seller seller) {
        SellerPreviewDTO dto = new SellerPreviewDTO();
        dto.setId(seller.getId());
        dto.setStoreName(seller.getStoreName());
        dto.setRating(seller.getRating());
        return dto;
    }

    public SellerFullDTO toSellerFull(Seller seller) {
        SellerFullDTO dto = new SellerFullDTO();
        dto.setId(seller.getId());
        dto.setStoreName(seller.getStoreName());
        dto.setEmail(seller.getEmail());
        dto.setPhone(seller.getPhone());
        dto.setRating(seller.getRating());
        dto.setCreatedAt(seller.getCreatedAt().toString());
        // Map the embedded Address entity to AddressDTO
        dto.setAddress(toAddressDTO(seller.getAddress()));
        return dto;
    }

    // CATEGORY MAPPING
    public String toCategoryString(Category category) {
        return category.getName(); // For the "Array of strings" requirement
    }

    // PRODUCT MAPPINGS
    public ProductPreviewDTO toProductPreview(Product product) {
        ProductPreviewDTO dto = new ProductPreviewDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setThumbnail(product.getThumbnail());
        dto.setCategory(product.getCategory().getName());
        return dto;
    }

    public ProductFullDTO toProductFull(Product product) {
        ProductFullDTO dto = new ProductFullDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        dto.setStock(product.getStock());
        dto.setThumbnail(product.getThumbnail());
        dto.setImages(product.getImages());
        dto.setCreatedAt(product.getCreatedAt().toString());

        // Relationship mapping
        dto.setSeller(toSellerPreview(product.getSeller()));

        CategoryDTO catDto = new CategoryDTO();
        catDto.setName(product.getCategory().getName());
        dto.setCategory(catDto);

        return dto;
    }

    // ORDER MAPPING
    public OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomerName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt().toString());

        // Convert the List<Product> entities into List<ProductPreviewDTO>
        List<ProductPreviewDTO> productPreviews = order.getProducts().stream()
                .map(this::toProductPreview)
                .collect(Collectors.toList());

        dto.setProducts(productPreviews);
        return dto;
    }

    // Helper for Address
    public AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setZipCode(address.getZipCode());
        return dto;
    }

    // --- TO ENTITY (DTO -> Entity) ---

    public Product toProductEntity(ProductFullDTO dto) {
        Product entity = new Product();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setStock(dto.getStock());
        entity.setImages(dto.getImages());
        entity.setThumbnail(dto.getThumbnail());
        // Relationships are handled by the Service to ensure DB integrity
        return entity;
    }

    public Seller toSellerEntity(SellerFullDTO dto) {
        Seller entity = new Seller();
        entity.setStoreName(dto.getStoreName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        if (dto.getAddress() != null) {
            entity.setAddress(toAddressEntity(dto.getAddress()));
        }
        return entity;
    }

    public Address toAddressEntity(AddressDTO dto) {
        Address entity = new Address();
        entity.setStreet(dto.getStreet());
        entity.setCity(dto.getCity());
        entity.setCountry(dto.getCountry());
        entity.setZipCode(dto.getZipCode());
        return entity;
    }
}