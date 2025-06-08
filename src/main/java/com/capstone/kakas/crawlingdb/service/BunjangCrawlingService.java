package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.dto.*;
import com.capstone.kakas.crawlingdb.dto.request.ProductCrawlingDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BunjangCrawlingService {

    private final ProductRepository productRepository;
    private final UsedPriceRepository usedPriceRepository;

    /**
     * 1단계: 크롤링 가능한 상품 매핑 정보 조회
     * @return 상품명과 번개장터 URL 매핑 리스트
     */
    public List<ProductCrawlingDto> getProductCrawlingMapping() {
        log.info("크롤링 대상 상품 매핑 정보 조회 시작");

        List<Product> products = productRepository.findByBunjangUrlIsNotNull();

        List<ProductCrawlingDto> mappingList = products.stream()
                .filter(product -> product.getBunjangUrl() != null && !product.getBunjangUrl().trim().isEmpty())
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("크롤링 대상 상품 수: {}", mappingList.size());
        return mappingList;
    }

    /**
     * 2단계: 실제 크롤링 실행 - 각 URL에서 판매제목과 가격 추출
     * @param crawlingTargets 크롤링 대상 상품 리스트
     * @return 크롤링된 원시 데이터 리스트
     */
    public List<CrawlingResultDto> executeCrawling(List<ProductCrawlingDto> crawlingTargets) {
        log.info("크롤링 실행 시작 - 대상 상품 수: {}", crawlingTargets.size());

        List<CrawlingResultDto> crawlingResults = new ArrayList<>();

        for (ProductCrawlingDto target : crawlingTargets) {
            try {
                log.debug("크롤링 시작 - 상품: {}, URL: {}", target.getProductName(), target.getBunjangUrl());

                // 실제 크롤링 수행
                List<BunjangItemDto> items = crawlBunjangUrl(target.getBunjangUrl());


                // 크롤링 결과 로그
                log.info("크롤링 완료 - 상품: {}, 수집된 항목 수: {}", target.getProductName(), items.size());

                // 크롤링된 항목들의 샘플 로그 (처음 3개만)
                for (int i = 0; i < Math.min(3, items.size()); i++) {
                    BunjangItemDto item = items.get(i);
                    log.debug("크롤링 샘플 [{}] - 제목: {}, 가격: {}", i+1, item.getTitle(), item.getPrice());
                }

                CrawlingResultDto result = CrawlingResultDto.builder()
                        .productId(target.getProductId())
                        .productName(target.getProductName())
                        .bunjangUrl(target.getBunjangUrl())
                        .items(items)
                        .crawledAt(LocalDateTime.now())
                        .build();

                crawlingResults.add(result);
                log.debug("크롤링 완료 - 상품: {}, 수집된 항목 수: {}", target.getProductName(), items.size());

                // 크롤링 간격 조절 (서버 부하 방지)
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("크롤링 실패 - 상품: {}, 오류: {}", target.getProductName(), e.getMessage(), e);
            }
        }

        log.info("크롤링 완료 - 처리된 상품 수: {}", crawlingResults.size());
        return crawlingResults;
    }





    /**
     * 3단계: 판매제목 필터링 - 상품에 부합하지 않는 항목 제거
     * @param crawlingResults 크롤링된 원시 데이터
     * @return 필터링된 데이터 리스트
     */
    public List<FilteredResultDto> filteringProductTitle(List<CrawlingResultDto> crawlingResults) {
        log.info("상품 제목 필터링 시작");

        List<FilteredResultDto> filteredResults = new ArrayList<>();

        for (CrawlingResultDto crawlingResult : crawlingResults) {
            try {
                log.debug("필터링 시작 - 상품: {}, 크롤링된 항목 수: {}",
                        crawlingResult.getProductName(), crawlingResult.getItems().size());

                // 크롤링된 항목들 로그 출력 (디버깅용)
                for (BunjangItemDto item : crawlingResult.getItems()) {
                    log.debug("크롤링된 항목 - 제목: {}, 가격: {}", item.getTitle(), item.getPrice());
                }

                // 필터링 키워드 설정 (상품별로 다르게 설정 가능)
                FilterKeywords filterKeywords = getFilterKeywords(crawlingResult.getProductName());

                log.debug("필터링 키워드 - 검색: {}, 제외: {}",
                        filterKeywords.getSearchKeywords(), filterKeywords.getExcludeKeywords());

                // 필터링 수행
                List<BunjangItemDto> filteredItems = new ArrayList<>();
                for (BunjangItemDto item : crawlingResult.getItems()) {
                    boolean isValid = isValidItem(item, filterKeywords);
                    log.debug("필터링 검사 - 제목: {}, 유효: {}", item.getTitle(), isValid);
                    if (isValid) {
                        filteredItems.add(item);
                    }
                }

                FilteredResultDto filteredResult = FilteredResultDto.builder()
                        .productId(crawlingResult.getProductId())
                        .productName(crawlingResult.getProductName())
                        .originalItemCount(crawlingResult.getItems().size())
                        .filteredItemCount(filteredItems.size())
                        .filteredItems(filteredItems)
                        .filteredAt(LocalDateTime.now())
                        .build();

                filteredResults.add(filteredResult);

                log.debug("필터링 완료 - 상품: {}, 원본: {}개 -> 필터링 후: {}개",
                        crawlingResult.getProductName(),
                        crawlingResult.getItems().size(),
                        filteredItems.size());

            } catch (Exception e) {
                log.error("필터링 실패 - 상품: {}, 오류: {}", crawlingResult.getProductName(), e.getMessage(), e);
            }
        }

        log.info("필터링 완료 - 처리된 상품 수: {}", filteredResults.size());
        return filteredResults;
    }

    /**
     * 4단계: 중고가격 계산 및 저장
     * @param filteredResults 필터링된 데이터
     * @return 처리 결과
     */
    public List<UsedPriceResultDto> calculateUsedPrice(List<FilteredResultDto> filteredResults) {
        log.info("중고가격 계산 및 저장 시작");

        List<UsedPriceResultDto> priceResults = new ArrayList<>();

        for (FilteredResultDto filteredResult : filteredResults) {
            try {
                if (filteredResult.getFilteredItems().isEmpty()) {
                    log.warn("필터링된 항목이 없어 가격 계산 불가 - 상품: {}", filteredResult.getProductName());
                    continue;
                }

                // 평균 가격 계산
                List<Integer> prices = filteredResult.getFilteredItems().stream()
                        .map(BunjangItemDto::getPrice)
                        .filter(price -> price > 0) // 0원 제외
                        .collect(Collectors.toList());

                if (prices.isEmpty()) {
                    log.warn("유효한 가격 정보가 없음 - 상품: {}", filteredResult.getProductName());
                    continue;
                }

                double averagePrice = prices.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

                int finalAveragePrice = (int) Math.round(averagePrice);

                // Product 조회
                Product product = productRepository.findById(filteredResult.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + filteredResult.getProductId()));

                // UsedPrice 엔티티 생성 및 저장
                UsedPrice usedPrice = UsedPrice.builder()
                        .product(product)
                        .price(finalAveragePrice)
//                        .sampleCount(prices.size())
                        .build();

                usedPriceRepository.save(usedPrice);

                UsedPriceResultDto priceResult = UsedPriceResultDto.builder()
                        .productId(filteredResult.getProductId())
                        .productName(filteredResult.getProductName())
                        .averagePrice(finalAveragePrice)
                        .sampleCount(prices.size())
                        .minPrice(Collections.min(prices))
                        .maxPrice(Collections.max(prices))
                        .calculatedAt(LocalDateTime.now())
                        .build();

                priceResults.add(priceResult);

                log.debug("가격 계산 완료 - 상품: {}, 평균가격: {}원, 샘플수: {}개",
                        filteredResult.getProductName(), finalAveragePrice, prices.size());

            } catch (Exception e) {
                log.error("가격 계산 실패 - 상품: {}, 오류: {}", filteredResult.getProductName(), e.getMessage(), e);
            }
        }

        log.info("중고가격 계산 완료 - 처리된 상품 수: {}", priceResults.size());
        return priceResults;
    }










    /**
     * 아이템이 필터링 조건에 맞는지 검사
     */
    private boolean isValidItem(BunjangItemDto item, FilterKeywords filterKeywords) {
        String title = item.getTitle().toLowerCase();

        // 검색 키워드 중 하나라도 포함되어야 함
//        boolean hasSearchKeyword = filterKeywords.getSearchKeywords().isEmpty() ||
//                filterKeywords.getSearchKeywords().stream()
//                        .anyMatch(keyword -> title.contains(keyword.toLowerCase()));

        boolean hasSearchKeyword = true;


        // 제외 키워드가 포함되면 안됨
        boolean hasExcludeKeyword = filterKeywords.getExcludeKeywords().stream()
                .anyMatch(keyword -> title.contains(keyword.toLowerCase()));

//        return hasSearchKeyword && !hasExcludeKeyword;
        return true;
    }





    /**
     * 상품별 필터링 키워드 설정
     */
    private FilterKeywords getFilterKeywords(String productName) {
        // 상품명에 따른 필터링 키워드 설정
        // 실제로는 DB나 설정 파일에서 관리할 수 있음

        List<String> searchKeywords = new ArrayList<>();
        List<String> excludeKeywords = new ArrayList<>();

        if (productName.toLowerCase().contains("플스5") || productName.toLowerCase().contains("ps5")) {
            searchKeywords.addAll(Arrays.asList("플스5", "ps5", "플레이스테이션5"));
            excludeKeywords.addAll(Arrays.asList("헤드셋", "컨트롤러", "게임", "액세서리", "케이스"));
        }
        // 다른 상품에 대한 키워드도 추가 가능

        return FilterKeywords.builder()
                .searchKeywords(searchKeywords)
                .excludeKeywords(excludeKeywords)
                .build();
    }



    /**
     * 실제 번개장터 크롤링 수행
     */
    private List<BunjangItemDto> crawlBunjangUrl(String bunjangUrl) {
        List<BunjangItemDto> items = new ArrayList<>();
        WebDriver driver = null;

        try {
            // WebDriver 설정
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 백그라운드 실행
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

            log.debug("번개장터 페이지 로딩 시작: {}", bunjangUrl);
            driver.get(bunjangUrl);

            // 페이지 로딩 대기
            Thread.sleep(3000);

            // 무한 스크롤을 위한 스크롤 다운 (번개장터는 무한스크롤 방식)
//            scrollToLoadMoreItems(driver);

            // 상품 아이템들 찾기
            List<WebElement> productElements = driver.findElements(By.cssSelector("div[data-testid='product-item']"));

            if (productElements.isEmpty()) {
                // 다른 셀렉터 시도
                productElements = driver.findElements(By.cssSelector(".sc-dlfnbm"));
                if (productElements.isEmpty()) {
                    productElements = driver.findElements(By.cssSelector("[class*='ProductItem']"));
                }
            }

            log.debug("찾은 상품 수: {}", productElements.size());

            for (WebElement element : productElements) {
                try {
                    BunjangItemDto item = extractItemInfo(element, driver);
                    if (item != null && item.getPrice() != null && item.getPrice() > 0) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    log.warn("개별 상품 정보 추출 실패: {}", e.getMessage());
                }
            }

            log.debug("성공적으로 추출된 상품 수: {}", items.size());

        } catch (Exception e) {
            log.error("번개장터 크롤링 실패: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("WebDriver 종료 중 오류: {}", e.getMessage());
                }
            }
        }

        return items;
    }

    /**
     * 개별 상품 정보 추출
     */
    private BunjangItemDto extractItemInfo(WebElement element, WebDriver driver) {
        try {
            String title = "";
            Integer price = null;
            String seller = "";
            String location = "";
            String itemUrl = "";

            // 제목 추출
            try {
                WebElement titleElement = element.findElement(By.cssSelector("[class*='title'], [class*='name'], h3, .sc-jSMfEi"));
                title = titleElement.getText().trim();
            } catch (Exception e) {
                // 다른 셀렉터 시도
                try {
                    WebElement titleElement = element.findElement(By.cssSelector("div:nth-child(2) > div:first-child"));
                    title = titleElement.getText().trim();
                } catch (Exception ex) {
                    log.debug("제목 추출 실패");
                }
            }

            // 가격 추출
            try {
                WebElement priceElement = element.findElement(By.cssSelector("[class*='price'], .sc-kgAjT, span[class*='Price']"));
                String priceText = priceElement.getText().replaceAll("[^0-9]", "");
                if (!priceText.isEmpty()) {
                    price = Integer.parseInt(priceText);
                }
            } catch (Exception e) {
                // 다른 방법으로 가격 추출 시도
                try {
                    List<WebElement> spanElements = element.findElements(By.tagName("span"));
                    for (WebElement span : spanElements) {
                        String text = span.getText();
                        if (text.contains("원") && text.matches(".*\\d.*")) {
                            String priceText = text.replaceAll("[^0-9]", "");
                            if (!priceText.isEmpty()) {
                                price = Integer.parseInt(priceText);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.debug("가격 추출 실패");
                }
            }

            // 판매자 정보 추출
            try {
                WebElement sellerElement = element.findElement(By.cssSelector("[class*='seller'], [class*='user'], .sc-kGXeez"));
                seller = sellerElement.getText().trim();
            } catch (Exception e) {
                seller = "판매자 정보 없음";
            }

            // 지역 정보 추출
            try {
                WebElement locationElement = element.findElement(By.cssSelector("[class*='location'], [class*='area'], .sc-jKJlTe"));
                location = locationElement.getText().trim();
            } catch (Exception e) {
                location = "지역 정보 없음";
            }

            // 상품 URL 추출
            try {
                WebElement linkElement = element.findElement(By.cssSelector("a"));
                String href = linkElement.getAttribute("href");
                if (href != null && href.startsWith("/")) {
                    itemUrl = "https://m.bunjang.co.kr" + href;
                } else if (href != null) {
                    itemUrl = href;
                }
            } catch (Exception e) {
                log.debug("URL 추출 실패");
            }

            // 유효한 데이터인지 확인
            if (title.isEmpty() || price == null) {
                return null;
            }

            return BunjangItemDto.builder()
                    .title(title)
                    .price(price)
                    .seller(seller)
                    .location(location)
                    .itemUrl(itemUrl)
                    .build();

        } catch (Exception e) {
            log.warn("상품 정보 추출 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Product 엔티티를 DTO로 변환
     */
    private ProductCrawlingDto convertToDto(Product product) {
        return ProductCrawlingDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .bunjangUrl(product.getBunjangUrl())
                .build();
    }

}
