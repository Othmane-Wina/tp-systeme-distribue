package com.demo.restapi.repository;
import com.demo.restapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    // Custom method to fetch all products sorted by creation date automatically
    List<Product> findAllByOrderByCreatedAtDesc();
}
