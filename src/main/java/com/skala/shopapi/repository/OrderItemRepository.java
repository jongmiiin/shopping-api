package com.skala.shopapi.repository;

import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.OrderItem;
import com.skala.shopapi.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByCustomer_CustomerId(String customerId);

    Optional<OrderItem> findByCustomerAndProduct(Customer customer, Product product);
}
