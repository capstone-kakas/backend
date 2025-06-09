package com.capstone.kakas.crawlingdb.dto;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BunjangItemDto {
    private String title;
    private Integer price;
    private String seller;
    private String location;
    private String itemUrl;
}
