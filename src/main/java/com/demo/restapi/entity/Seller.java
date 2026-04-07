package com.demo.restapi.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String storeName;

    @Email
    private String email;

    private String phone;

    @Embedded
    private Address address;

    @Min(0) @Max(5)
    private double rating;

    private LocalDateTime createdAt = LocalDateTime.now(); // [cite: 53]

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Product> products;
}
