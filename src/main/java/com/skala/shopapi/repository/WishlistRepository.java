package com.skala.shopapi.repository;

import com.skala.shopapi.entity.Customer;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.entity.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByCustomer_CustomerId(String customerId);

    Optional<Wishlist> findByCustomerAndProduct(Customer customer, Product product);

    boolean existsByCustomerAndProduct(Customer customer, Product product);
}
