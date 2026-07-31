package com.skala.shopapi.service;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.common.SessionHandler;
import com.skala.shopapi.dto.CustomerSession;
import com.skala.shopapi.dto.OrderItemDto;
import com.skala.shopapi.dto.OrderListDto;
import com.skala.shopapi.dto.OrderRequest;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.OrderItem;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ParameterException;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.CustomerRepository;
import com.skala.shopapi.repository.OrderItemRepository;
import com.skala.shopapi.repository.ProductRepository;
import com.skala.shopapi.tools.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final double INITIAL_POINT = 1_000_000.0;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final SessionHandler sessionHandler;

    public Response<PagedList<Customer>> getAllCustomers(int offset, int count) {
        Page<Customer> page = customerRepository.findAll(PageRequest.of(offset, count));
        return Response.success(PagedList.of(page, offset, count));
    }

    @Transactional(readOnly = true)
    public Response<OrderListDto> getCustomerById(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));

        List<OrderItem> orderItems = orderItemRepository.findByCustomer_CustomerId(customerId);
        List<OrderItemDto> products = orderItems.stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getProductName())
                        .productPrice(item.getProduct().getProductPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderListDto dto = OrderListDto.builder()
                .customerId(customer.getCustomerId())
                .customerPoint(customer.getCustomerPoint())
                .products(products)
                .build();
        return Response.success(dto);
    }

    public Response<Customer> createCustomer(Customer customer) {
        if (StringUtil.isAnyEmpty(customer.getCustomerId(), customer.getCustomerPassword())) {
            throw new ParameterException("customerId", "customerPassword");
        }

        if (customerRepository.existsById(customer.getCustomerId())) {
            throw new ResponseException(Error.DATA_DUPLICATED);
        }

        customer.setCustomerPoint(INITIAL_POINT);
        Customer saved = customerRepository.save(customer);
        return Response.success(saved);
    }

    public Response<Customer> updateCustomer(Customer customer) {
        if (StringUtil.isAnyEmpty(customer.getCustomerId()) || customer.getCustomerPoint() == null) {
            throw new ResponseException(Error.DATA_NOT_FOUND, "Invalid customer data");
        }

        Customer existing = customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        existing.setCustomerPoint(customer.getCustomerPoint());
        Customer saved = customerRepository.save(existing);
        return Response.success(saved);
    }

    public Response<Void> deleteCustomer(Customer customer) {
        Customer existing = customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        customerRepository.delete(existing);
        return Response.success(null);
    }

    public Response<Customer> loginCustomer(CustomerSession customerSession, HttpServletResponse response) {
        if (StringUtil.isAnyEmpty(customerSession.getCustomerId(), customerSession.getCustomerPassword())) {
            throw new ParameterException("customerId", "customerPassword");
        }

        Customer customer = customerRepository.findById(customerSession.getCustomerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        if (!customer.getCustomerPassword().equals(customerSession.getCustomerPassword())) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        sessionHandler.storeAccessToken(response, customer.getCustomerId());
        customer.setCustomerPassword(null);
        return Response.success(customer);
    }

    @Transactional
    public Response<Customer> placeOrder(OrderRequest order, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        double cost = product.getProductPrice() * order.getQuantity();
        if (customer.getCustomerPoint() < cost) {
            throw new ResponseException(Error.INSUFFICIENT_FUNDS);
        }
        customer.setCustomerPoint(customer.getCustomerPoint() - cost);

        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElse(null);
        if (orderItem == null) {
            orderItemRepository.save(new OrderItem(customer, product, order.getQuantity()));
        } else {
            orderItem.setQuantity(orderItem.getQuantity() + order.getQuantity());
        }

        return Response.success(customer);
    }

    @Transactional
    public Response<Customer> cancelOrder(OrderRequest order, HttpServletRequest request) {
        String customerId = sessionHandler.getCurrentCustomerId(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        if (orderItem.getQuantity() < order.getQuantity()) {
            throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
        }

        int remaining = orderItem.getQuantity() - order.getQuantity();
        if (remaining == 0) {
            orderItemRepository.delete(orderItem);
        } else {
            orderItem.setQuantity(remaining);
        }

        double refund = product.getProductPrice() * order.getQuantity();
        customer.setCustomerPoint(customer.getCustomerPoint() + refund);

        return Response.success(customer);
    }
}
