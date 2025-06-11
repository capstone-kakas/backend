package com.capstone.kakas.crawlingdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceListDto;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceResultDto;
import com.capstone.kakas.crawlingdb.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class PriceController {

    private final ProductService productService;

    @GetMapping("/salePrices")
    @Operation(summary = "단일 판매가 조회 api",description = "제공된 상품이름으로 가격 검색")
    public ApiResponse<String> getSalePrice(
            @RequestParam("productName") String productName
    ) {
        String response = productService.getSalePrice(productName);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/usedPrices")
    @Operation(summary = "단일 중고거래가 조회 api",description = "제공된 상품이름으로 가격 검색")
    public ApiResponse<UsedPriceResultDto> getUsedPrice(
            @RequestParam("productName") String productName
    ) {
        UsedPriceResultDto response = productService.getUsedPrice(productName);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/usedPrices/list")
    @Operation(summary = "일자별 중고거래가 조회 api",description = "제공된 상품이름으로 일자별 가격 검색")
    public ApiResponse<List<UsedPriceListDto>> getUsedPriceList(
            @RequestParam("productName") String productName
    ) {
        List<UsedPriceListDto> response = productService.getUsedPriceList(productName);
        return ApiResponse.onSuccess(response);
    }



}
