package com.demo.restapi.dto;

import lombok.Data;

@Data
public class SellerFullDTO {
    private String id;
    private String storeName;
    private String email;
    private String phone;
    private AddressDTO address;
    private double rating;
    private String createdAt;
}
