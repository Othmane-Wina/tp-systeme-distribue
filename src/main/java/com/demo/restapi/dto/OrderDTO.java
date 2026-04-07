package com.demo.restapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private String id;
    private String customerName;
    private List<ProductPreviewDTO> products; // Array of Product Previews
    private double totalAmount;
    private String status;
    private String createdAt;
}
