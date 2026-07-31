package com.skala.shopapi.controller;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.dto.CustomerSession;
import com.skala.shopapi.dto.OrderListDto;
import com.skala.shopapi.dto.OrderRequest;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Tag(name = "Customer", description = "고객 관리·인증·주문 API")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "고객 목록 조회 (페이징)")
    @GetMapping("/list")
    public Response<PagedList<Customer>> getAllCustomers(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        return customerService.getAllCustomers(offset, count);
    }

    @Operation(summary = "고객 상세 조회 (보유 주문상품 포함)")
    @GetMapping("/{customerId}")
    public Response<OrderListDto> getCustomerById(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId);
    }

    @Operation(summary = "회원가입 (초기 포인트 지급)")
    @PostMapping
    public Response<Customer> createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @Operation(summary = "고객 정보 수정")
    @PutMapping
    public Response<Customer> updateCustomer(@RequestBody Customer customer) {
        return customerService.updateCustomer(customer);
    }

    @Operation(summary = "고객 삭제")
    @DeleteMapping
    public Response<Void> deleteCustomer(@RequestBody Customer customer) {
        return customerService.deleteCustomer(customer);
    }

    @Operation(summary = "로그인 (세션 쿠키 발급)")
    @PostMapping("/login")
    public Response<Customer> loginCustomer(@RequestBody CustomerSession customerSession,
            HttpServletResponse response) {
        return customerService.loginCustomer(customerSession, response);
    }

    @Operation(summary = "상품 주문 (로그인 필요, 포인트 차감)")
    @PostMapping("/order")
    public Response<Customer> placeOrder(@RequestBody OrderRequest order, HttpServletRequest request) {
        return customerService.placeOrder(order, request);
    }

    @Operation(summary = "주문 취소 (포인트 환급)")
    @PostMapping("/cancel")
    public Response<Customer> cancelOrder(@RequestBody OrderRequest order, HttpServletRequest request) {
        return customerService.cancelOrder(order, request);
    }
}
