package com.demo.restapi.controller;

import com.demo.restapi.dto.ListResponseDTO;
import com.demo.restapi.dto.ProductFullDTO;
import com.demo.restapi.dto.ProductPreviewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.demo.restapi.service.ProductService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "/api/v1/products",
        produces = { "application/json", "application/xml" }
)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1.a & 1.e: Get list with Pagination & Sorting
    @GetMapping
    public ResponseEntity<ListResponseDTO<EntityModel<ProductPreviewDTO>>> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. Get a Page object from the service (contains only 5 items, not the whole DB)
        Page<ProductPreviewDTO> pageResult = productService.searchProducts(q, category, minPrice, pageable);

        // 2. Convert each DTO into an EntityModel with a "self" link
        List<EntityModel<ProductPreviewDTO>> resources = pageResult.getContent().stream()
                .map(p -> EntityModel.of(p,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class).getProductById(p.getId())).withSelfRel()))
                .collect(Collectors.toList());

        // 3. Build the response wrapper
        ListResponseDTO<EntityModel<ProductPreviewDTO>> response = new ListResponseDTO<>();
        response.setData(resources);
        response.setTotal(pageResult.getTotalElements()); // Real total count from DB
        response.setPage(pageResult.getNumber());
        response.setLimit(pageResult.getSize());

        return ResponseEntity.ok(response);
    }

    // 1.b: GET is safe and idempotent
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductFullDTO>> getProductById(@PathVariable String id) {
        // 1. Fetch data from service
        ProductFullDTO product = productService.getProductById(id);

        // 2. Build the HATEOAS Model (Level 3 Maturity)
        EntityModel<ProductFullDTO> resource = EntityModel.of(product,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .getProductById(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getProducts(null, null, null, PageRequest.of(0, 10)))
                        .withRel("collection"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SellerController.class)
                        .getSellerById(product.getSeller().getId())).withRel("seller")
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
                // Retirez .eTag(...) ici
                .body(resource);
    }

    // Version 2 (Header-based)
    // This method is only called if the header "X-API-VERSION=2" is present
    @GetMapping(value = "/{id}", headers = "X-API-VERSION=2")
    public EntityModel<ProductFullDTO> getProductByIdV2(@PathVariable String id) {
        ProductFullDTO product = productService.getProductById(id);

        // Imagine in V2 we change something, like adding a "deprecated" warning
        // or a different data structure.
        product.setName(product.getName() + " (V2 Optimized)");

        return EntityModel.of(product,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .getProductByIdV2(id)).withSelfRel());
    }

    // 1.b: POST is NOT idempotent
    @PostMapping
    public ResponseEntity<EntityModel<ProductFullDTO>> createProduct(@Valid @RequestBody ProductFullDTO productDTO) {
        ProductFullDTO createdProduct = productService.createProduct(productDTO);

        EntityModel<ProductFullDTO> resource = EntityModel.of(createdProduct,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .getProductById(createdProduct.getId())).withSelfRel(),

                // Fixed Collection link
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                                .getProducts(null, null, null, PageRequest.of(0, 10)))
                        .withRel("collection")
        );

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // 1.b: PATCH for partial updates (price/stock)
    @PatchMapping("/{id}")
    public ResponseEntity<ProductFullDTO> updateProduct(@PathVariable String id, @RequestBody ProductFullDTO updateDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}