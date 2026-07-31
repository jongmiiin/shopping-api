package com.skala.shopapi.controller;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.dto.ReviewDto;
import com.skala.shopapi.dto.ReviewListDto;
import com.skala.shopapi.dto.ReviewRequest;
import com.skala.shopapi.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "상품 리뷰·평점 API")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "상품 리뷰 목록 조회 (평균 평점 포함)")
    @GetMapping("/api/products/{productId}/reviews")
    public Response<ReviewListDto> getReviews(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @Operation(summary = "상품 리뷰 작성")
    @PostMapping("/api/products/{productId}/reviews")
    public Response<ReviewDto> createReview(@PathVariable Long productId, @RequestBody ReviewRequest review,
            HttpServletRequest request) {
        return reviewService.createReview(productId, review, request);
    }

    @Operation(summary = "리뷰 삭제 (작성자 본인만 가능)")
    @DeleteMapping("/api/reviews/{reviewId}")
    public Response<Void> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        return reviewService.deleteReview(reviewId, request);
    }
}
