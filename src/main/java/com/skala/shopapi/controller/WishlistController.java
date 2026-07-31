package com.skala.shopapi.controller;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.dto.ProductIdRequest;
import com.skala.shopapi.dto.WishlistItemDto;
import com.skala.shopapi.service.WishlistService;
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

@Tag(name = "Wishlist", description = "위시리스트/찜하기 API")
@RestController
@RequestMapping("/api/customers/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "내 위시리스트 조회")
    @GetMapping
    public Response<List<WishlistItemDto>> getWishlist(HttpServletRequest request) {
        return wishlistService.getWishlist(request);
    }

    @Operation(summary = "위시리스트에 추가 (찜하기)")
    @PostMapping
    public Response<Void> addWishlist(@RequestBody ProductIdRequest req, HttpServletRequest request) {
        return wishlistService.addWishlist(req, request);
    }

    @Operation(summary = "위시리스트에서 제거")
    @DeleteMapping
    public Response<Void> removeWishlist(@RequestBody ProductIdRequest req, HttpServletRequest request) {
        return wishlistService.removeWishlist(req, request);
    }
}
