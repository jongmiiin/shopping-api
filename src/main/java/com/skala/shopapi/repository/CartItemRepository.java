package com.skala.shopapi.repository;

import com.skala.shopapi.entity.CartItem;
import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomer_CustomerId(String customerId);

    Optional<CartItem> findByCustomerAndProduct(Customer customer, Product product);
}
