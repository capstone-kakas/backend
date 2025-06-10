package com.capstone.kakas.crawlingdb.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingResultDto {
    private Long productId;
    private String productName;
    private String Url;
    private String price;
    private String SaleTitle;
}
