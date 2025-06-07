package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.dto.BunjangItemDto;
import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
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
