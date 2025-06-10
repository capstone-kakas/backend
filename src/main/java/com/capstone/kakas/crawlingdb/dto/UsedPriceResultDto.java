package com.capstone.kakas.crawlingdb.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedPriceResultDto {
    private Long productId;
    private String productName;
    private Integer averagePrice;
    private Integer sampleCount;
    private Integer minPrice;
    private Integer maxPrice;
}
