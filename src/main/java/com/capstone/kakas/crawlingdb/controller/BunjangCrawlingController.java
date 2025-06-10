package com.capstone.kakas.crawlingdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
import com.capstone.kakas.crawlingdb.dto.FilteredResultDto;
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
    public ApiResponse<List<CrawlingResultDto>> executeCrawling() {
        // 크롤링 가능한 상품 매핑 정보 조회
        List<ProductCrawlingDto> crawlingTargets = bunjangCrawlingService.getProductCrawlingMapping();
        // 매핑된 상품,url을 사용하여 각 url에서 판매제목과 가격을 크롤링
        List<CrawlingResultDto> crawlingResult = bunjangCrawlingService.executeCrawling(crawlingTargets);
        // 크롤링된 판매제목
        List<CrawlingResultDto> filteredResult = titleFilteringService.filteringTitle(crawlingResult);



        //        // 크롤링된 판매제목과 가격에서 판매제목이 상품에 부합하는지 필터링
//        // 상품이름(예: 플스5 프로)으로부터 중고상품들을 검색하더라도 중고거래사이트 특성상 알맞지 않은 상품에 대한 판매정보가 포함될 수 있음(예: 플스 5 프로 + 펄스 헤드셋 )
//        // 단일 상품에 대한 중고판매가를 원하기 때문에 간단한 searchKeyword나 ExcludeKeyword를 사용하여 추가적인 필터링
//        List<FilteredResultDto> filteredResult = bunjangCrawlingService.filteringProductTitle(crawlingResult);
//        //필터링을 거친 가격들의 평균들을 구해 Product의 연관 entity인 UsedPrice에 가격 저장 + 크롤링시간(createdAt)
//        List<UsedPriceResultDto> usedPriceResult = bunjangCrawlingService.calculateUsedPrice(filteredResult);

        return ApiResponse.onSuccess(filteredResult);
    }
}
