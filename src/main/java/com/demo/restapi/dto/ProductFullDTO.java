package com.demo.restapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductFullDTO {
    private String id;
    private String name;
    private String description;
    private double price;
    private String currency;
    private int stock;
    private List<String> images;
    private String thumbnail;
    private CategoryDTO category; // Lab specifies object for full
    private String createdAt;
    private SellerPreviewDTO seller;
}
