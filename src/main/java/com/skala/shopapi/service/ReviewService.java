package com.skala.shopapi.service;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.common.SessionHandler;
import com.skala.shopapi.dto.ReviewDto;
import com.skala.shopapi.dto.ReviewListDto;
import com.skala.shopapi.dto.ReviewRequest;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.OrderHistory;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.entity.Review;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ParameterException;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.CustomerRepository;
import com.skala.shopapi.repository.OrderHistoryRepository;
import com.skala.shopapi.repository.ProductRepository;
import com.skala.shopapi.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final SessionHandler sessionHandler;

    @Transactional
    public Response<ReviewDto> createReview(Long productId, ReviewRequest req, HttpServletRequest request) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new ParameterException("rating");
        }

        String customerId = sessionHandler.getCurrentCustomerId(request);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        boolean hasPurchased = orderHistoryRepository.existsByCustomer_CustomerIdAndProduct_IdAndType(
                customerId, productId, OrderHistory.TYPE_ORDER);
        if (!hasPurchased) {
            throw new ResponseException(Error.PURCHASE_REQUIRED);
        }

        Review saved = reviewRepository.save(new Review(customer, product, req.getRating(), req.getComment()));
        return Response.success(toReviewDto(saved));
    }

    @Transactional(readOnly = true)
    public Response<ReviewListDto> getReviewsByProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        List<ReviewDto> reviews = reviewRepository.findByProduct_Id(productId).stream()
                .map(this::toReviewDto)
                .collect(Collectors.toList());
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);

        ReviewListDto dto = ReviewListDto.builder()
                .averageRating(averageRating)
                .reviews(reviews)
                .build();
        return Response.success(dto);
    }

    @Transactional
    public Response<Void> deleteReview(Long reviewId, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new ResponseException(Error.NOT_AUTHENTICATED, "Not the review owner");
        }

        reviewRepository.delete(review);
        return Response.success(null);
    }

    private ReviewDto toReviewDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getCustomerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
