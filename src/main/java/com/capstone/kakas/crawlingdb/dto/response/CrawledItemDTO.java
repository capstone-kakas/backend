package com.capstone.kakas.crawlingdb.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@Builder
public class CrawledItemDTO {
    private final String title;       // 상품 제목
    private final BigDecimal price;   // 가격
}
