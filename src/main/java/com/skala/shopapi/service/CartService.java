package com.skala.shopapi.service;

import com.skala.shopapi.common.Response;
import com.skala.shopapi.common.SessionHandler;
import com.skala.shopapi.dto.CartItemDto;
import com.skala.shopapi.dto.OrderRequest;
import com.skala.shopapi.entity.CartItem;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.CartItemRepository;
import com.skala.shopapi.repository.CustomerRepository;
import com.skala.shopapi.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SessionHandler sessionHandler;
    private final CustomerService customerService;

    public Response<List<CartItemDto>> getCart(HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        List<CartItemDto> items = cartItemRepository.findByCustomer_CustomerId(customerId).stream()
                .map(item -> CartItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getProductName())
                        .productPrice(item.getProduct().getProductPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        return Response.success(items);
    }

    @Transactional
    public Response<Void> addToCart(OrderRequest req, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product).orElse(null);
        if (cartItem == null) {
            cartItemRepository.save(new CartItem(customer, product, req.getQuantity()));
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + req.getQuantity());
        }
        return Response.success(null);
    }

    @Transactional
    public Response<Void> removeFromCart(OrderRequest req, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        if (cartItem.getQuantity() < req.getQuantity()) {
            throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
        }

        int remaining = cartItem.getQuantity() - req.getQuantity();
        if (remaining == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(remaining);
        }
        return Response.success(null);
    }

    @Transactional
    public Response<Customer> checkout(HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);
        List<CartItem> cartItems = cartItemRepository.findByCustomer_CustomerId(customerId);

        Response<Customer> lastResult = null;
        for (CartItem cartItem : cartItems) {
            OrderRequest orderRequest = new OrderRequest(cartItem.getProduct().getId(), cartItem.getQuantity());
            lastResult = customerService.placeOrder(orderRequest, request);
        }

        cartItemRepository.deleteAll(cartItems);

        if (lastResult == null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
            return Response.success(customer);
        }
        return lastResult;
    }
}
