package com.skala.shopapi.controller;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.dto.CartItemDto;
import com.skala.shopapi.dto.OrderRequest;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/api/customers/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "내 장바구니 조회")
    @GetMapping
    public Response<List<CartItemDto>> getCart(HttpServletRequest request) {
        return cartService.getCart(request);
    }

    @Operation(summary = "장바구니 담기")
    @PostMapping
    public Response<Void> addToCart(@RequestBody OrderRequest order, HttpServletRequest request) {
        return cartService.addToCart(order, request);
    }

    @Operation(summary = "장바구니 항목 제거")
    @DeleteMapping
    public Response<Void> removeFromCart(@RequestBody OrderRequest order, HttpServletRequest request) {
        return cartService.removeFromCart(order, request);
    }

    @Operation(summary = "장바구니 전체 주문 확정")
    @PostMapping("/checkout")
    public Response<Customer> checkout(HttpServletRequest request) {
        return cartService.checkout(request);
    }
}
