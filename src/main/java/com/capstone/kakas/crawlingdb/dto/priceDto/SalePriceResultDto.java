package com.capstone.kakas.crawlingdb.dto.priceDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalePriceResultDto {
    private Long productId;
    private String productName;

    // 각 사이트별 가격 정보 (사이트명을 키로 사용)
    private String GMARKET;      // G마켓 가격
    private String ELEVENSTREET; // 11번가 가격
    private String COUPANG;      // 쿠팡 가격
    private String AUCTION;      // 옥션 가격
}
