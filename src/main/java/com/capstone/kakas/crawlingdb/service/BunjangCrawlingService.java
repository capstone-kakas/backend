package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.dto.*;
import com.capstone.kakas.crawlingdb.dto.request.ProductCrawlingDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openqa.selenium.NoSuchElementException;
import java.time.Duration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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



    public List<CrawlingResultDto> executeCrawling(List<ProductCrawlingDto> crawlingTargets) {
        List<CrawlingResultDto> results = new ArrayList<>();

        WebDriver driver = null;
        try {
            // WebDriverManager를 사용한 Chrome 드라이버 설정
            WebDriverManager.chromedriver()
                    .setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
            );

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            for (ProductCrawlingDto target : crawlingTargets) {
                try {
                    // 해당 URL로 이동
                    driver.get(target.getUrl());

                    // 페이지 로딩 대기
                    Thread.sleep(2000);

                    // Selector 유효성 검사
                    if (target.getTitleSelector() == null || target.getPriceSelector() == null) {
                        log.warn("Selector가 null입니다 - ProductId: {}", target.getProductId());
                        continue;
                    }

                    // 상품 목록의 공통 부모 요소 찾기 (selector에서 공통 부분 추출)
                    String parentSelector = extractParentSelector(target.getTitleSelector());
                    List<WebElement> productElements = driver.findElements(By.cssSelector(parentSelector));

                    for (WebElement productElement : productElements) {
                        try {
                            // 동적 selector를 사용하여 제목 추출
                            String relativeTitleSelector = extractRelativeSelector(target.getTitleSelector());
                            WebElement titleElement = productElement.findElement(By.cssSelector(relativeTitleSelector));
                            String title = titleElement.getText().trim();

                            // 동적 selector를 사용하여 가격 추출
                            String relativePriceSelector = extractRelativeSelector(target.getPriceSelector());
                            WebElement priceElement = productElement.findElement(By.cssSelector(relativePriceSelector));
                            String price = priceElement.getText().trim();

                            // 빈 값 체크
                            if (!title.isEmpty() && !price.isEmpty()) {
                                CrawlingResultDto result = new CrawlingResultDto();
                                result.setProductId(target.getProductId());
                                result.setProductName(target.getProductName());
                                result.setSaleTitle(title);
                                result.setPrice(price);
                                result.setUrl(target.getUrl());

                                results.add(result);
                            }

                        } catch (NoSuchElementException e) {
                            // 개별 상품 요소를 찾지 못한 경우 건너뛰기
                            continue;
                        }
                    }

                } catch (Exception e) {
                    // 해당 URL 처리 실패 시 로그 출력 후 다음으로 넘어가기
                    log.error("크롤링 실패 - URL: {}, ProductId: {}, Error: {}",
                            target.getUrl(), target.getProductId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("WebDriver 초기화 실패: {}", e.getMessage());
        } finally {
            // WebDriver 리소스 정리
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.error("WebDriver 종료 중 오류: {}", e.getMessage());
                }
            }
        }

        return results;
    }
















    /**
     * 전체 selector에서 상품 목록의 공통 부모 selector 추출
     * 예: "#root > div > div > div:nth-child(4) > div > div.sc-gbzWSY.dLcZgG > div > div:nth-child(3) > a > div.sc-kZmsYB.gshoXx > div.sc-RcBXQ.kWzERy"
     * → "#root > div > div > div:nth-child(4) > div > div.sc-gbzWSY.dLcZgG > div > div"
     */
    private String extractParentSelector(String fullSelector) {
        // 상품 리스트 컨테이너까지만 추출 (일반적으로 div > div 까지)
        String[] parts = fullSelector.split(" > ");
        StringBuilder parentSelector = new StringBuilder();

        // 상품 목록 컨테이너 레벨까지만 포함 (경험적으로 조정 필요)
        int endIndex = Math.min(parts.length - 4, parts.length); // 마지막 4개 요소 제외
        for (int i = 0; i < endIndex; i++) {
            if (i > 0) parentSelector.append(" > ");
            parentSelector.append(parts[i]);
        }

        return parentSelector.toString();
    }

    /**
     * 전체 selector에서 개별 상품 내의 상대 selector 추출
     * 예: "#root > div > div > div:nth-child(4) > div > div.sc-gbzWSY.dLcZgG > div > div:nth-child(3) > a > div.sc-kZmsYB.gshoXx > div.sc-RcBXQ.kWzERy"
     * → "a > div.sc-kZmsYB.gshoXx > div.sc-RcBXQ.kWzERy"
     */
    private String extractRelativeSelector(String fullSelector) {
        String[] parts = fullSelector.split(" > ");
        StringBuilder relativeSelector = new StringBuilder();

        // 마지막 몇 개 요소만 상대 selector로 사용
        int startIndex = Math.max(0, parts.length - 4); // 마지막 4개 요소 사용
        for (int i = startIndex; i < parts.length; i++) {
            if (i > startIndex) relativeSelector.append(" > ");
            relativeSelector.append(parts[i]);
        }

        return relativeSelector.toString();
    }















    /**
     * Product 엔티티를 DTO로 변환
     */
    private ProductCrawlingDto convertToDto(Product product) {
        return ProductCrawlingDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .Url(product.getBunjangUrl())
                .priceSelector(product.getPriceSelector())
                .titleSelector(product.getTitleSelector())
                .build();
    }

}
