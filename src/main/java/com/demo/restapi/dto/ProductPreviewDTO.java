package com.demo.restapi.dto;

import lombok.Data;

@Data
public class ProductPreviewDTO {
    private String id;
    private String name;
    private double price;
    private String thumbnail;
    private String category; // Lab specifies string for preview
}
