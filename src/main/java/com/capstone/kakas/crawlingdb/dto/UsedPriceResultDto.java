package com.capstone.kakas.crawlingdb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
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
