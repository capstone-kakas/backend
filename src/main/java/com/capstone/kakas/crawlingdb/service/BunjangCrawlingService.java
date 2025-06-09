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
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
            );

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            for (ProductCrawlingDto target : crawlingTargets) {
                try {
                    log.info("크롤링 시작 - ProductId: {}, URL: {}", target.getProductId(), target.getUrl());

                    // 해당 URL로 이동
                    driver.get(target.getUrl());

                    // 페이지 로딩 대기
                    Thread.sleep(3000);

                    // 번개장터 상품 목록을 동적으로 찾기
                    List<CrawlingResultDto> pageResults = crawlBunjangProducts(driver, target);
                    results.addAll(pageResults);

                    log.info("크롤링 완료 - ProductId: {}, 수집된 상품 수: {}", target.getProductId(), pageResults.size());

                } catch (Exception e) {
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
     * 번개장터 상품 목록을 동적으로 크롤링
     */
    private List<CrawlingResultDto> crawlBunjangProducts(WebDriver driver, ProductCrawlingDto target) {
        List<CrawlingResultDto> results = new ArrayList<>();

        try {
            // 번개장터의 여러 가능한 상품 컨테이너 selector들
            String[] containerSelectors = {
                    "div[class*='ProductList'] > div",
                    "div[class*='product'] > a",
                    "a[href*='/products/']",
                    "[data-pid]",
                    "div[class*='sc-'] > div > a" // 스타일드 컴포넌트 기반
            };

            List<WebElement> productElements = new ArrayList<>();

            // 각 selector를 시도해서 상품 목록 찾기
            for (String containerSelector : containerSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(containerSelector));
                    if (!elements.isEmpty()) {
                        log.info("상품 목록 발견 - Selector: {}, 개수: {}", containerSelector, elements.size());
                        productElements = elements;
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            // fallback: 더 광범위한 검색
            if (productElements.isEmpty()) {
                productElements = findProductElementsByPattern(driver);
            }

            log.info("총 발견된 상품 요소 수: {}", productElements.size());

            // 각 상품 요소에서 제목과 가격 추출
            for (int i = 0; i < Math.min(productElements.size(), 10); i++) { // 처음 10개만 디버깅
                try {
                    WebElement productElement = productElements.get(i);

                    // 디버깅: HTML 구조 출력
                    try {
                        String innerHTML = productElement.getAttribute("innerHTML");
                        log.debug("상품 요소 {} HTML: {}", i, innerHTML.length() > 500 ? innerHTML.substring(0, 500) + "..." : innerHTML);
                        log.debug("상품 요소 {} 텍스트: '{}'", i, productElement.getText());
                    } catch (Exception e) {
                        log.debug("HTML 구조 출력 실패");
                    }

                    // 제목 추출 시도
                    String title = extractTitle(productElement);
                    log.debug("추출된 제목 {}: '{}'", i, title);

                    // 가격 추출 시도
                    String price = extractPrice(productElement);
                    log.debug("추출된 가격 {}: '{}'", i, price);

                    // 유효한 데이터가 있을 때만 결과에 추가
                    if (isValidProductData(title, price)) {
                        CrawlingResultDto result = new CrawlingResultDto();
                        result.setProductId(target.getProductId());
                        result.setProductName(target.getProductName());
                        result.setSaleTitle(title);
                        result.setPrice(price);
                        result.setUrl(target.getUrl());

                        results.add(result);
                        log.info("상품 추가 - 제목: '{}', 가격: '{}'", title, price);
                    } else {
                        log.debug("유효하지 않은 상품 데이터 - 제목: '{}', 가격: '{}'", title, price);
                    }

                } catch (Exception e) {
                    log.debug("상품 요소 처리 실패 - index: {}, error: {}", i, e.getMessage());
                    continue;
                }
            }

            // 나머지 상품들은 디버그 로그 없이 처리
            for (int i = 10; i < productElements.size(); i++) {
                try {
                    WebElement productElement = productElements.get(i);

                    String title = extractTitle(productElement);
                    String price = extractPrice(productElement);

                    if (isValidProductData(title, price)) {
                        CrawlingResultDto result = new CrawlingResultDto();
                        result.setProductId(target.getProductId());
                        result.setProductName(target.getProductName());
                        result.setSaleTitle(title);
                        result.setPrice(price);
                        result.setUrl(target.getUrl());

                        results.add(result);
                    }

                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            log.error("번개장터 크롤링 중 오류: {}", e.getMessage());
        }

        return results;
    }

    /**
     * 패턴 기반으로 상품 요소들 찾기
     */
    private List<WebElement> findProductElementsByPattern(WebDriver driver) {
        List<WebElement> elements = new ArrayList<>();

        try {
            // 링크 기반 검색 (번개장터 상품은 보통 링크로 되어있음)
            List<WebElement> linkElements = driver.findElements(By.cssSelector("a"));

            for (WebElement link : linkElements) {
                String href = link.getAttribute("href");
                if (href != null && (href.contains("/products/") || href.contains("pid="))) {
                    // 상품 링크인 경우만 추가
                    elements.add(link);
                }
            }

            // 최대 50개로 제한 (너무 많은 요소 방지)
            if (elements.size() > 50) {
                elements = elements.subList(0, 50);
            }

        } catch (Exception e) {
            log.error("패턴 기반 상품 요소 검색 실패: {}", e.getMessage());
        }

        return elements;
    }

    /**
     * 상품 요소에서 제목 추출
     */
    private String extractTitle(WebElement productElement) {
        // 번개장터 모바일 전용 selector들
        String[] titleSelectors = {
                "div[class*='sc-'][class*='title']",
                "div[class*='sc-']:nth-child(1)",
                "div[class*='sc-']:first-child",
                "[class*='title']",
                "[class*='name']",
                "[class*='product']",
                "div > div:first-child",
                "div:first-child",
                "p:first-child",
                "span:first-child"
        };

        for (String selector : titleSelectors) {
            try {
                List<WebElement> titleElements = productElement.findElements(By.cssSelector(selector));
                for (WebElement titleElement : titleElements) {
                    String text = titleElement.getText().trim();
                    log.debug("제목 후보 발견 - Selector: {}, Text: '{}'", selector, text);
                    if (isValidTitle(text)) {
                        log.debug("유효한 제목 선택: '{}'", text);
                        return text;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        // fallback 1: 모든 div, p, span 요소의 텍스트 확인
        try {
            List<WebElement> textElements = productElement.findElements(By.cssSelector("div, p, span"));
            for (WebElement element : textElements) {
                String text = element.getText().trim();
                if (isValidTitle(text)) {
                    log.debug("fallback으로 제목 발견: '{}'", text);
                    return text;
                }
            }
        } catch (Exception e) {
            log.debug("fallback 제목 추출 실패: {}", e.getMessage());
        }

        // fallback 2: 전체 텍스트에서 첫 번째 유효한 라인 사용
        try {
            String allText = productElement.getText().trim();
            log.debug("전체 텍스트: '{}'", allText);

            if (!allText.isEmpty()) {
                String[] lines = allText.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (isValidTitle(line)) {
                        log.debug("라인에서 제목 발견: '{}'", line);
                        return line;
                    }
                }

                // 첫 번째 라인이 비어있지 않으면 사용
                if (lines.length > 0 && !lines[0].trim().isEmpty()) {
                    String firstLine = lines[0].trim();
                    if (firstLine.length() > 2 && firstLine.length() < 200) {
                        log.debug("첫 번째 라인을 제목으로 사용: '{}'", firstLine);
                        return firstLine;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("전체 텍스트 분석 실패: {}", e.getMessage());
        }

        log.debug("제목 추출 실패");
        return "";
    }

    /**
     * 상품 요소에서 가격 추출
     */
    private String extractPrice(WebElement productElement) {
        // 번개장터 모바일 전용 가격 selector들
        String[] priceSelectors = {
                "div[class*='sc-'][class*='price']",
                "div[class*='sc-']:last-child",
                "div[class*='sc-']:nth-last-child(1)",
                "div[class*='sc-']:nth-last-child(2)",
                "[class*='price']",
                "[class*='cost']",
                "[class*='won']",
                "div:last-child",
                "p:last-child",
                "span:last-child"
        };

        for (String selector : priceSelectors) {
            try {
                List<WebElement> priceElements = productElement.findElements(By.cssSelector(selector));
                for (WebElement priceElement : priceElements) {
                    String text = priceElement.getText().trim();
                    log.debug("가격 후보 발견 - Selector: {}, Text: '{}'", selector, text);
                    if (isValidPrice(text)) {
                        log.debug("유효한 가격 선택: '{}'", text);
                        return text;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        // fallback 1: 모든 텍스트 요소에서 가격 패턴 찾기
        try {
            List<WebElement> textElements = productElement.findElements(By.cssSelector("div, p, span"));
            for (WebElement element : textElements) {
                String text = element.getText().trim();
                if (isValidPrice(text)) {
                    log.debug("fallback으로 가격 발견: '{}'", text);
                    return text;
                }
            }
        } catch (Exception e) {
            log.debug("fallback 가격 추출 실패: {}", e.getMessage());
        }

        // fallback 2: 전체 텍스트에서 가격 패턴 찾기
        try {
            String allText = productElement.getText();
            log.debug("가격 검색을 위한 전체 텍스트: '{}'", allText);

            // 다양한 가격 패턴 매칭
            String[] pricePatterns = {
                    "\\d+[,\\d]*원",           // 1,000원
                    "\\d+[,\\d]*만원",         // 10만원
                    "\\d+[,\\d]*\\s*원",       // 1000 원
                    "\\d+[,\\d]*$",           // 숫자만
                    "원\\s*\\d+[,\\d]*",       // 원 1000
                    "₩\\s*\\d+[,\\d]*"        // ₩ 1000
            };

            for (String patternStr : pricePatterns) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
                java.util.regex.Matcher matcher = pattern.matcher(allText);

                while (matcher.find()) {
                    String foundPrice = matcher.group().trim();
                    if (isValidPrice(foundPrice)) {
                        log.debug("패턴으로 가격 발견: '{}'", foundPrice);
                        return foundPrice;
                    }
                }
            }

            // 라인별로 검사
            String[] lines = allText.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (isValidPrice(line)) {
                    log.debug("라인에서 가격 발견: '{}'", line);
                    return line;
                }
            }

        } catch (Exception e) {
            log.debug("패턴 기반 가격 추출 실패: {}", e.getMessage());
        }

        log.debug("가격 추출 실패");
        return "";
    }

    /**
     * 유효한 제목인지 확인 (조건 완화)
     */
    private boolean isValidTitle(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }

        // 길이 조건 완화
        if (title.length() < 2 || title.length() > 300) {
            return false;
        }

        // 가격 패턴이 아닌지 확인 (조건 완화)
        if (title.matches("^\\d+[,\\d]*원?$") ||
                title.matches("^[0-9,\\s]+$") ||
                title.matches("^\\d+만원$")) {
            return false;
        }

        // 의미있는 텍스트인지 확인
        if (title.matches("^[\\s\\-_=.]+$")) {
            return false;
        }

        return true;
    }

    /**
     * 유효한 가격인지 확인 (조건 완화)
     */
    private boolean isValidPrice(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }

        // 다양한 가격 패턴 허용
        return price.matches(".*\\d+[,\\d]*원.*") ||
                price.matches(".*\\d+[,\\d]*만원.*") ||
                price.matches(".*\\d+[,\\d]*\\s*원.*") ||
                price.matches("^\\d+[,\\d]*$") ||  // 숫자만
                price.contains("원") ||
                price.contains("₩") ||
                (price.matches(".*\\d+.*") && price.length() < 20); // 숫자 포함하고 짧은 텍스트
    }

    /**
     * 유효한 상품 데이터인지 확인
     */
    private boolean isValidProductData(String title, String price) {
        return isValidTitle(title) && isValidPrice(price);
    }






}
