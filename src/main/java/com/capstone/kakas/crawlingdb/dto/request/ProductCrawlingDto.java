package com.capstone.kakas.crawlingdb.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCrawlingDto {
    private Long productId;
    private String productName;
    private String Url;
    private String titleSelector;
    private String priceSelector;
}
