package com.skala.shopapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderHistory {

    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_CANCEL = "CANCEL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private Double amount;

    private String type;

    private LocalDateTime orderedAt;

    public OrderHistory(Customer customer, Product product, Integer quantity, Double amount, String type) {
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
        this.type = type;
        this.orderedAt = LocalDateTime.now();
    }
}
