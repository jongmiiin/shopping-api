package com.skala.shopapi.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDto {

    private Long id;
    private String productName;
    private Integer quantity;
    private Double amount;
    private String type;
    private LocalDateTime orderedAt;
}
