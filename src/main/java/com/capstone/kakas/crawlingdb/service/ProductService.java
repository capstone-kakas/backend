package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.dto.UsedPriceResultDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.SalePriceRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UsedPriceRepository usedPriceRepository;
    private final SalePriceRepository salePriceRepository;

    public UsedPriceResultDto getUsedPrice(String productName) {
        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new TempHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        UsedPrice latestPrice = usedPriceRepository.findTopByProductOrderByCreatedAtDesc(product)
                .orElseThrow(() -> new TempHandler(ErrorStatus.USED_PRICE_NOT_FOUND));

        UsedPriceResultDto usedPriceResultDto = UsedPriceResultDto.builder()
                .productId(product.getId())
                .productName(productName)
                .minPrice(latestPrice.getMinPrice())
                .maxPrice(latestPrice.getMaxPrice())
                .averagePrice(latestPrice.getPrice())
                .sampleCount(latestPrice.getSampleCount())
                .build();
        return usedPriceResultDto;
    }


    public String getSalePrice(String productName) {

        return null;
    }
}
