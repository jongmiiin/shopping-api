package com.skala.shopapi.repository;

import com.skala.shopapi.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductName(String productName);

    Page<Product> findByProductNameContainingAndCategory_Id(String keyword, Long categoryId, Pageable pageable);

    Page<Product> findByProductNameContaining(String keyword, Pageable pageable);

    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);

    List<Product> findByCategory_Id(Long categoryId);
}
