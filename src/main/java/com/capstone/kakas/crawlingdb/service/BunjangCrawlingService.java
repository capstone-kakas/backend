package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.dto.*;
import com.capstone.kakas.crawlingdb.dto.request.ProductCrawlingDto;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.UsedPriceRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    String title = extractTitle(productElement, driver);
                    log.info("추출된 제목 {}: '{}'", i, title);

                    // 가격 추출 시도
                    String price = extractPrice(productElement, driver);
                    log.info("추출된 가격 {}: '{}'", i, price);

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

                    String title = extractTitle(productElement, driver);
                    String price = extractPrice(productElement, driver);

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
     * 상품 요소에서 제목 추출 (개선된 버전)
     */
    private String extractTitle(WebElement productElement, WebDriver driver) {
        // 1. 광고 요소 제외
        try {
            String elementText = productElement.getText();
            if (elementText.contains("AD") || elementText.contains("광고") || elementText.contains("매입")) {
                log.debug("광고 요소 발견, 제목 추출 건너뜀");
                return "";
            }
        } catch (Exception e) {
            // 무시하고 계속 진행
        }

        // 2. 번개장터 모바일 전용 제목 selector들 (우선순위 순)
        String[] titleSelectors = {
                // 메인 제목 컨테이너
                "div[class*='title']:not([class*='price']):not([class*='time'])",
                "div[class*='name']:not([class*='price']):not([class*='time'])",
                "div[class*='product']:not([class*='price']):not([class*='time'])",
                // 구조적 접근
                "div[class*='sc-'] > div:first-child:not([class*='price']):not([class*='time'])",
                "div[class*='sc-']:first-child:not([class*='price']):not([class*='time'])",
                // 일반적인 패턴
                "h1, h2, h3, h4, h5, h6",
                "p:first-child:not([class*='price']):not([class*='time'])",
                "span:first-child:not([class*='price']):not([class*='time'])"
        };

        // 각 selector로 제목 찾기
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
                log.debug("Selector {} 실행 중 오류: {}", selector, e.getMessage());
                continue;
            }
        }

        // 3. fallback: 모든 텍스트 요소에서 제목 후보 찾기
        try {
            List<WebElement> textElements = productElement.findElements(
                    By.cssSelector("div:not([class*='price']):not([class*='time']), p:not([class*='price']):not([class*='time']), span:not([class*='price']):not([class*='time'])")
            );

            for (WebElement element : textElements) {
                String text = element.getText().trim();
                if (isValidTitle(text) && !isTimePattern(text) && !isValidPrice(text)) {
                    log.debug("fallback으로 제목 발견: '{}'", text);
                    return text;
                }
            }
        } catch (Exception e) {
            log.debug("fallback 제목 추출 실패: {}", e.getMessage());
        }

        // 4. 최종 fallback: 전체 텍스트에서 첫 번째 유효한 라인 사용
        try {
            String allText = productElement.getText().trim();
            log.debug("전체 텍스트 분석: '{}'", allText);

            if (!allText.isEmpty()) {
                String[] lines = allText.split("\n");
                for (String line : lines) {
                    line = line.trim();

                    // 시간, 가격, 배송비 등의 패턴 제외
                    if (isValidTitle(line) &&
                            !isTimePattern(line) &&
                            !isValidPrice(line) &&
                            !isDeliveryPattern(line) &&
                            !isLocationPattern(line)) {
                        log.debug("라인에서 제목 발견: '{}'", line);
                        return line;
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
     * 상품 요소에서 가격 추출 (개선된 버전)
     */
    private String extractPrice(WebElement productElement, WebDriver driver) {
        // 1. CSS ::after를 고려한 가격 추출 시도
        try {
            // JavaScript로 ::after content 추출 시도
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String afterContent = (String) js.executeScript(
                    "return window.getComputedStyle(arguments[0], '::after').getPropertyValue('content');",
                    productElement
            );

            if (afterContent != null && !afterContent.equals("none") && !afterContent.equals("\"\"")) {
                String cleanPrice = afterContent.replaceAll("[\"']", "").trim();
                if (isValidPrice(cleanPrice)) {
                    log.debug("::after에서 가격 발견: '{}'", cleanPrice);
                    return cleanPrice;
                }
            }
        } catch (Exception e) {
            log.debug("::after 추출 실패: {}", e.getMessage());
        }

        // 2. 번개장터 모바일 전용 가격 selector들
        String[] priceSelectors = {
                // 가격 전용 클래스
                "[class*='price']:not(span):not([class*='time'])",
                "[class*='cost']:not(span):not([class*='time'])",
                "[class*='won']:not(span):not([class*='time'])",
                // 구조적 접근 (가격은 보통 마지막에 위치)
                "div[class*='sc-']:last-child:not([class*='time'])",
                "div[class*='sc-']:nth-last-child(1):not([class*='time'])",
                "div[class*='sc-']:nth-last-child(2):not([class*='time'])",
                // CSS 가상요소를 가진 요소들
                "div[class*='sc-'][data-price]",
                "div[style*='after']",
                // fallback
                "strong:not([class*='time'])",
                "b:not([class*='time'])"
        };

        for (String selector : priceSelectors) {
            try {
                List<WebElement> priceElements = productElement.findElements(By.cssSelector(selector));
                for (WebElement priceElement : priceElements) {
                    String text = priceElement.getText().trim();
                    log.debug("가격 후보 발견 - Selector: {}, Text: '{}'", selector, text);

                    if (isValidPrice(text) && !isTimePattern(text)) {
                        log.debug("유효한 가격 선택: '{}'", text);
                        return text;
                    }

                    // 빈 텍스트지만 CSS로 표시되는 가격일 수 있음
                    if (text.isEmpty()) {
                        try {
                            String computedStyle = ((JavascriptExecutor) driver).executeScript(
                                    "return window.getComputedStyle(arguments[0]).getPropertyValue('content');",
                                    priceElement
                            ).toString();

                            if (isValidPrice(computedStyle)) {
                                log.debug("CSS content에서 가격 발견: '{}'", computedStyle);
                                return computedStyle.replaceAll("[\"']", "");
                            }
                        } catch (Exception e) {
                            // 무시하고 계속
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Selector {} 실행 중 오류: {}", selector, e.getMessage());
                continue;
            }
        }

        // 3. 패턴 기반 가격 추출
        try {
            String allText = productElement.getText();
            log.debug("가격 검색을 위한 전체 텍스트: '{}'", allText);

            // 한국어 가격 패턴들 (우선순위 순)
            String[] pricePatterns = {
                    "\\d{1,3}(?:,\\d{3})*원",           // 1,000원, 10,000원
                    "\\d{1,3}(?:,\\d{3})*만원",         // 10만원, 100만원
                    "\\d+만\\s*\\d*천?원",              // 10만 5천원
                    "\\d+천원",                        // 5천원
                    "\\d{1,3}(?:,\\d{3})*\\s*원",       // 1000 원 (공백 포함)
                    "원\\s*\\d{1,3}(?:,\\d{3})*",       // 원 1000
                    "₩\\s*\\d{1,3}(?:,\\d{3})*",        // ₩ 1000
                    "\\d{4,}(?!분|시간|일|개월|년)",      // 4자리 이상 숫자 (시간 단위 제외)
            };

            for (String patternStr : pricePatterns) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
                java.util.regex.Matcher matcher = pattern.matcher(allText);

                while (matcher.find()) {
                    String foundPrice = matcher.group().trim();
                    if (isValidPrice(foundPrice) && !isTimePattern(foundPrice)) {
                        log.debug("패턴으로 가격 발견: '{}'", foundPrice);
                        return foundPrice;
                    }
                }
            }

            // 라인별 검사 (시간 패턴 제외)
            String[] lines = allText.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (isValidPrice(line) && !isTimePattern(line) && !isDeliveryPattern(line)) {
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
     * 유효한 제목인지 검증 (개선된 버전)
     */
    private boolean isValidTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        text = text.trim();

        // 길이 체크
        if (text.length() < 2 || text.length() > 200) {
            return false;
        }

        // 제외할 패턴들
        String[] excludePatterns = {
                "^\\d+분\\s*전$",                    // "15분 전"
                "^\\d+시간\\s*전$",                  // "2시간 전"
                "^\\d+일\\s*전$",                    // "3일 전"
                "^\\d+개월\\s*전$",                  // "1개월 전"
                "^방금$",                           // "방금"
                "^배송비\\s*포함$",                  // "배송비포함"
                "^무료배송$",                       // "무료배송"
                "^택배비\\s*별도$",                  // "택배비별도"
                "^직거래$",                         // "직거래"
                "^\\d{1,3}(?:,\\d{3})*원$",         // 가격 패턴
                "^\\d+만원$",                       // 만원 단위
                "^찜\\s*\\d+$",                     // "찜 123"
                "^조회\\s*\\d+$",                   // "조회 456"
                "^AD$",                            // "AD"
                "^광고$",                          // "광고"
                "^SOLD$",                          // "SOLD"
                "^판매완료$"                        // "판매완료"
        };

        for (String pattern : excludePatterns) {
            if (text.matches(pattern)) {
                return false;
            }
        }

        // 한글, 영문, 숫자, 기본 특수문자만 허용
        if (!text.matches("[가-힣a-zA-Z0-9\\s\\-_.,()\\[\\]{}+&/]*")) {
            return false;
        }

        return true;
    }

    /**
     * 유효한 가격인지 검증 (개선된 버전)
     */
    private boolean isValidPrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        text = text.trim().replaceAll("[\"']", ""); // 따옴표 제거

        // 시간 패턴 체크 (가격이 아님)
        if (isTimePattern(text)) {
            return false;
        }

        // 배송 관련 패턴 체크 (가격이 아님)
        if (isDeliveryPattern(text)) {
            return false;
        }

        // 유효한 가격 패턴들
        String[] validPricePatterns = {
                "^\\d{1,3}(?:,\\d{3})*원$",         // 1,000원
                "^\\d{1,3}(?:,\\d{3})*만원$",       // 10만원
                "^\\d+만\\s*\\d*천?원$",            // 10만 5천원
                "^\\d+천원$",                      // 5천원
                "^\\d{1,3}(?:,\\d{3})*$",          // 숫자만 (1000, 10000 등)
                "^₩\\s*\\d{1,3}(?:,\\d{3})*$"      // ₩1000
        };

        for (String pattern : validPricePatterns) {
            if (text.matches(pattern)) {
                // 숫자만 있는 경우 최소값 체크 (너무 작은 숫자는 가격이 아닐 가능성)
                if (text.matches("^\\d+$")) {
                    try {
                        int price = Integer.parseInt(text.replaceAll(",", ""));
                        return price >= 100; // 최소 100원
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 시간 패턴인지 확인
     */
    private boolean isTimePattern(String text) {
        if (text == null) return false;

        String[] timePatterns = {
                "^\\d+분\\s*전$",
                "^\\d+시간\\s*전$",
                "^\\d+일\\s*전$",
                "^\\d+개월\\s*전$",
                "^\\d+년\\s*전$",
                "^방금$",
                "^just now$",
                "^\\d+m ago$",
                "^\\d+h ago$",
                "^\\d+d ago$"
        };

        for (String pattern : timePatterns) {
            if (text.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 배송 관련 패턴인지 확인
     */
    private boolean isDeliveryPattern(String text) {
        if (text == null) return false;

        String[] deliveryPatterns = {
                "^배송비\\s*포함$",
                "^배송비\\s*별도$",
                "^무료배송$",
                "^택배비\\s*포함$",
                "^택배비\\s*별도$",
                "^직거래$",
                "^직거래\\s*가능$",
                "^택배거래$",
                "^반값택배$"
        };

        for (String pattern : deliveryPatterns) {
            if (text.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 지역 패턴인지 확인
     */
    private boolean isLocationPattern(String text) {
        if (text == null) return false;

        // 간단한 지역 패턴 체크
        return text.matches("^[가-힣]+시$|^[가-힣]+구$|^[가-힣]+동$|^[가-힣]+읍$|^[가-힣]+면$");
    }









    /**
     * 유효한 상품 데이터인지 확인
     */
    private boolean isValidProductData(String title, String price) {
        return isValidTitle(title) && isValidPrice(price);
    }






}
