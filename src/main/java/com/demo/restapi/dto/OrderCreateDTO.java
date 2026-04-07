package com.demo.restapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    private String customerName;
    private List<String> products; // Array of product IDs
    private double totalAmount;
    private AddressDTO shippingAddress;
}
