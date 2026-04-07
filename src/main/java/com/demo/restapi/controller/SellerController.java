package com.demo.restapi.controller;

import com.demo.restapi.dto.SellerFullDTO;
import com.demo.restapi.dto.SellerPreviewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.demo.restapi.service.SellerService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/sellers") // 1.f Versioning technique: URI
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // 1.a: Get list (Granularity: Returns Preview)
    @GetMapping
    public CollectionModel<EntityModel<SellerPreviewDTO>> getAllSellers() {
        List<EntityModel<SellerPreviewDTO>> sellers = sellerService.getAllSellers().stream()
                .map(s -> EntityModel.of(s,
                        linkTo(methodOn(SellerController.class).getSellerById(s.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return CollectionModel.of(sellers,
                linkTo(methodOn(SellerController.class).getAllSellers()).withSelfRel());
    }

    // 1.a: Get by ID (Granularity: Returns Full)
    @GetMapping("/{id}")
    public EntityModel<SellerFullDTO> getSellerById(@PathVariable String id) {
        SellerFullDTO seller = sellerService.getSellerById(id);

        return EntityModel.of(seller,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SellerController.class)
                        .getSellerById(id)).withSelfRel(),
                // Fixed link to the product collection
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getProducts(null, null, null, PageRequest.of(0, 10)))
                        .withRel("products_by_seller")
        );
    }

    // 1.b: POST - Create (Not Idempotent)
    @PostMapping
    public ResponseEntity<EntityModel<SellerFullDTO>> createSeller(@Valid @RequestBody SellerFullDTO sellerDTO) {
        SellerFullDTO createdSeller = sellerService.createSeller(sellerDTO);

        EntityModel<SellerFullDTO> resource = EntityModel.of(createdSeller,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SellerController.class)
                        .getSellerById(createdSeller.getId())).withSelfRel(),
                // Fixed link
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getProducts(null, null, null, PageRequest.of(0, 10)))
                        .withRel("all-products")
        );

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // 1.b: PATCH - Update (Partial update, email forbidden logic in Service)
    @PatchMapping("/{id}")
    public ResponseEntity<SellerFullDTO> updateSeller(@PathVariable String id, @RequestBody SellerFullDTO updateDTO) {
        return ResponseEntity.ok(sellerService.updateSeller(id, updateDTO));
    }

    // 1.b: DELETE - Idempotent
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@PathVariable String id) {
        sellerService.deleteSeller(id);
    }
}
