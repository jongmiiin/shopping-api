package com.skala.shopapi.controller;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회 (페이징)")
    @GetMapping("/list")
    public Response<PagedList<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer count) {
        return productService.getAllProducts(offset, count);
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/{id}")
    public Response<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @Operation(summary = "상품 등록")
    @PostMapping
    public Response<Product> createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @Operation(summary = "상품 정보 수정")
    @PutMapping
    public Response<Product> updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping
    public Response<Void> deleteProduct(@RequestBody Product product) {
        return productService.deleteProduct(product);
    }

    @Operation(summary = "상품 검색 (키워드/카테고리 조합)")
    @GetMapping("/search")
    public Response<PagedList<Product>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer count) {
        return productService.searchProducts(keyword, categoryId, offset, count);
    }
}
