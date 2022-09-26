package com.luzko.warehouse.web.controller;

import com.luzko.warehouse.dto.BlockProductRequestDto;
import com.luzko.warehouse.dto.ProductRequestDto;
import com.luzko.warehouse.dto.ProductResponseDto;
import com.luzko.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        final var response = productService.getAllProducts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/products/selected")
    public ResponseEntity<List<ProductResponseDto>> getSelectedProducts(@RequestBody List<String> productCodes) {
        final var response = productService.getSelectedProducts(productCodes);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> placeProducts(
            @Valid @RequestBody List<ProductRequestDto> productRequestDtos) {
        final var response = productService.placeProducts(productRequestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/products/block")
    public ResponseEntity<List<ProductResponseDto>> blockProduct(
            @Valid @RequestBody List<BlockProductRequestDto> blockProductRequestDtos) {
        final var response =
                productService.updateProducts(true, blockProductRequestDtos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/unblock")
    public ResponseEntity<List<ProductResponseDto>> unblockProduct(
            @Valid @RequestBody List<BlockProductRequestDto> blockProductRequestDtos) {
        final var response =
                productService.updateProducts(false, blockProductRequestDtos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/depart")
    public ResponseEntity<List<ProductResponseDto>> departProduct(
            @Valid @RequestBody List<BlockProductRequestDto> blockProductRequestDtos) {
        final var response =
                productService.departProducts(blockProductRequestDtos);
        return ResponseEntity.ok(response);
    }
}
