package com.capstone.kakas.crawlingdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.crawlingdb.converter.CrawlingProductConverter;
import com.capstone.kakas.crawlingdb.dto.response.CrawlingProductResponse;
import com.capstone.kakas.crawlingdb.repository.CrawlingProductRepositoy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/crawling")
@RequiredArgsConstructor
public class CrawlingProductController {

    private final CrawlingProductRepositoy crawlingProductRepository;

    @GetMapping("/product")
    public ApiResponse<List<CrawlingProductResponse.CrawlingProductDTO>> getAllCrawlingProducts() {
        return ApiResponse.onSuccess(
                CrawlingProductConverter.toCrawlingProductDTOList(crawlingProductRepository.findAll())
        );
    }
}
