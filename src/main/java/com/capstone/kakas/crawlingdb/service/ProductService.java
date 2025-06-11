package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceListDto;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceResultDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.SalePriceRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<UsedPriceListDto> getUsedPriceList(String productName) {
        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new TempHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        List<UsedPrice> usedPriceList = usedPriceRepository.findAllByProduct(product);

        return usedPriceList.stream()
                .map(up -> UsedPriceListDto.builder()
                        .productId(product.getId())
                        .productName(productName)
                        .averagePrice(up.getPrice())
                        .sampleCount(up.getSampleCount())
                        .minPrice(up.getMinPrice())
                        .maxPrice(up.getMaxPrice())
                        .createdAt(LocalDate.from(up.getCrawledAt()))
                        .build()
                )
                .collect(Collectors.toList());
    }



    public String getSalePrice(String productName) {

        return null;
    }

    public String getSalePriceMonth(String productName) {

        return null;
    }
}
