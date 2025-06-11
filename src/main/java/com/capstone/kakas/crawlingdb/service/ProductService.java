package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.SalePrice;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.dto.priceDto.SalePriceResultDto;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceListDto;
import com.capstone.kakas.crawlingdb.dto.priceDto.UsedPriceResultDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.SalePriceRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
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



    public SalePriceResultDto getSalePrice(String productName) {
        // 1. productName으로 Product 찾기
        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productName));

        // 2. 해당 Product의 가장 최신 SalePrice들 조회 (각 사이트별로 가장 최근 것)
        List<SalePrice> latestSalePrices = salePriceRepository.findLatestSalePricesByProduct(product.getId());

        // 3. 사이트별로 가격 정보 매핑 및 포맷팅
        String gmarketPrice = null;
        String elevenstreetPrice = null;
        String coupangPrice = null;
        String auctionPrice = null;

        for (SalePrice salePrice : latestSalePrices) {
            String formattedPrice = formatPrice(salePrice.getPrice());

            switch (salePrice.getSiteName()) {
                case GMARKET:
                    gmarketPrice = formattedPrice;
                    break;
                case ELEVENSTREET:
                    elevenstreetPrice = formattedPrice;
                    break;
                case COUPANG:
                    coupangPrice = formattedPrice;
                    break;
                case AUCTION:
                    auctionPrice = formattedPrice;
                    break;
            }
        }

        // 4. DTO 생성 및 반환
        return SalePriceResultDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .GMARKET(gmarketPrice)
                .ELEVENSTREET(elevenstreetPrice)
                .COUPANG(coupangPrice)
                .AUCTION(auctionPrice)
                .build();
    }
    private String formatPrice(int price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    public String getSalePriceMonth(String productName) {

        return null;
    }
}
