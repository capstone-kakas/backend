package com.capstone.kakas.crawlingdb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingResultDto {
    private Long productId;
    private String productName;
    private String bunjangUrl;
    private List<BunjangItemDto> items;
    private LocalDateTime crawledAt;
}
