package com.capstone.kakas.crawlingdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceResultDto;
import com.capstone.kakas.crawlingdb.dto.request.ProductCrawlingDto;
import com.capstone.kakas.crawlingdb.service.BunjangCrawlingService;
import com.capstone.kakas.crawlingdb.service.TitleFilteringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/crawling")
@RequiredArgsConstructor
@Slf4j
public class BunjangCrawlingController {

    private final BunjangCrawlingService bunjangCrawlingService;
    private final TitleFilteringService titleFilteringService;

    @PostMapping("/execute")
    public ApiResponse<List<UsedPriceResultDto>> executeCrawling() {
        // 크롤링 가능한 상품 매핑 정보 조회
        List<ProductCrawlingDto> crawlingTargets = bunjangCrawlingService.getProductCrawlingMapping();
        // 매핑된 상품,url을 사용하여 각 url에서 판매제목과 가격을 크롤링
        List<CrawlingResultDto> crawlingResult = bunjangCrawlingService.executeCrawling(crawlingTargets);

        // 크롤링된 판매제목
        // 1. exclude keyword filtering
        List<CrawlingResultDto> excludeFilteredResult = titleFilteringService.filteringExcludeKeyword(crawlingResult);
        // 2. include keyword filtering
        List<CrawlingResultDto> includeFilteredResult = titleFilteringService.filteringIncludeKeyword(excludeFilteredResult);
        // 3. alias replacement
        List<CrawlingResultDto> replaceAliasResult = titleFilteringService.replaceAlias(includeFilteredResult);
        // 4. 유사도비교 filtering 및 저장
        List<UsedPriceResultDto> filteredResult = titleFilteringService.cosineSimilarityFiltering(replaceAliasResult);


        return ApiResponse.onSuccess(filteredResult);
    }
}
