package com.skala.shopapi.repository;

import com.skala.shopapi.entity.OrderHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByCustomer_CustomerIdOrderByOrderedAtDesc(String customerId);
}
