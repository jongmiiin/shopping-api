package com.skala.shopapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    private String customerId;

    private String customerPassword;

    private Double customerPoint;

    public Customer(String customerId, Double customerPoint) {
        this.customerId = customerId;
        this.customerPoint = customerPoint;
    }
}
