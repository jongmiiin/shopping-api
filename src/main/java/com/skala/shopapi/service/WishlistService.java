package com.skala.shopapi.service;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.common.SessionHandler;
import com.skala.shopapi.dto.ProductIdRequest;
import com.skala.shopapi.dto.WishlistItemDto;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.entity.Wishlist;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.CustomerRepository;
import com.skala.shopapi.repository.ProductRepository;
import com.skala.shopapi.repository.WishlistRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SessionHandler sessionHandler;

    public Response<List<WishlistItemDto>> getWishlist(HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        List<WishlistItemDto> items = wishlistRepository.findByCustomer_CustomerId(customerId).stream()
                .map(wishlist -> WishlistItemDto.builder()
                        .productId(wishlist.getProduct().getId())
                        .productName(wishlist.getProduct().getProductName())
                        .productPrice(wishlist.getProduct().getProductPrice())
                        .build())
                .collect(Collectors.toList());
        return Response.success(items);
    }

    @Transactional
    public Response<Void> addWishlist(ProductIdRequest req, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        if (wishlistRepository.existsByCustomerAndProduct(customer, product)) {
            throw new ResponseException(Error.DATA_DUPLICATED);
        }

        wishlistRepository.save(new Wishlist(customer, product));
        return Response.success(null);
    }

    @Transactional
    public Response<Void> removeWishlist(ProductIdRequest req, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Wishlist wishlist = wishlistRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        wishlistRepository.delete(wishlist);
        return Response.success(null);
    }
}
