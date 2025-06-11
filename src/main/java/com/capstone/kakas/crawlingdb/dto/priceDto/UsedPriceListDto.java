package com.capstone.kakas.crawlingdb.dto.priceDto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedPriceListDto {
    private Long productId;
    private String productName;
    private Integer averagePrice;
    private Integer sampleCount;
    private Integer minPrice;
    private Integer maxPrice;
    private LocalDate createdAt;
}
