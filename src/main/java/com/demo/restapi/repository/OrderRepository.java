package com.demo.restapi.repository;
import com.demo.restapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Requirement: Get by customer name
    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);

    // Requirement: Get by product ID
    // Since Order has a List<Product>, JPA can navigate the relationship
    List<Order> findByProducts_Id(String productId);
}
