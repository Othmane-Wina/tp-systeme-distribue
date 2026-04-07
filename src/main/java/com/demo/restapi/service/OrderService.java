package com.demo.restapi.service;

import com.demo.restapi.dto.OrderCreateDTO;
import com.demo.restapi.dto.OrderDTO;
import com.demo.restapi.entity.Order;
import com.demo.restapi.entity.OrderStatus;
import com.demo.restapi.entity.Product;
import com.demo.restapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.demo.restapi.mapper.DTOMapper;
import org.springframework.stereotype.Service;
import com.demo.restapi.repository.OrderRepository;
import com.demo.restapi.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DTOMapper mapper;

    public OrderDTO createOrder(OrderCreateDTO createDTO) {
        Order order = new Order();
        order.setCustomerName(createDTO.getCustomerName());
        order.setStatus(OrderStatus.PENDING);

        // Fetch products from the IDs provided in the DTO
        List<Product> products = createDTO.getProducts().stream()
                .map(id -> productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found")))
                .collect(Collectors.toList());

        order.setProducts(products);

        // Logic: Calculate total internally (don't trust the client's totalAmount!)
        double calculatedTotal = products.stream().mapToDouble(Product::getPrice).sum();
        order.setTotalAmount(calculatedTotal);

        return mapper.toOrderDTO(orderRepository.save(order));
    }

    public List<OrderDTO> getOrdersByCustomer(String name) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(name).stream()
                .map(mapper::toOrderDTO)
                .collect(Collectors.toList());
    }
}
