package com.capstone.kakas.crawlingdb.dto.priceDto;

import lombok.*;

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
