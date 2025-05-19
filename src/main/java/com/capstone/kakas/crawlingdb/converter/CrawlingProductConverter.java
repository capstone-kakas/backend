package com.capstone.kakas.crawlingdb.converter;

import com.capstone.kakas.crawlingdb.domain.CrawlingProduct;
import com.capstone.kakas.crawlingdb.dto.response.CrawlingProductResponse;

import java.util.List;
import java.util.stream.Collectors;

public class CrawlingProductConverter {

    public static CrawlingProductResponse.CrawlingProductDTO toDTO(CrawlingProduct product) {
        return CrawlingProductResponse.CrawlingProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .build();
    }

    public static List<CrawlingProductResponse.CrawlingProductDTO> toCrawlingProductDTOList(List<CrawlingProduct> products) {
        return products.stream()
                .map(CrawlingProductConverter::toDTO)
                .collect(Collectors.toList());
    }
}
