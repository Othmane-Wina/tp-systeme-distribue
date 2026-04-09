package com.demo.restapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ProductFullDTO {
    private String id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @NotBlank(message = "Currency is required (e.g., USD)")
    private String currency;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private List<String> images;
    private String thumbnail;

    @NotNull(message = "Category is required")
    private CategoryDTO category;

    private String createdAt;

    @NotNull(message = "Seller information is required") // <--- THIS FIXES YOUR TEST
    private SellerPreviewDTO seller;
}