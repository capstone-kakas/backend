package com.capstone.kakas.crawlingdb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BunjangItemDto {
    private String title;
    private Integer price;
    private String seller;
    private String location;
    private String itemUrl;
}
