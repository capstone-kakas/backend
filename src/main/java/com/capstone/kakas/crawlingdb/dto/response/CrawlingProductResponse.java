package com.capstone.kakas.crawlingdb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class CrawlingProductResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CrawlingProductDTO {
        private Long id;
        private String name;
        private String category;
    }
}
