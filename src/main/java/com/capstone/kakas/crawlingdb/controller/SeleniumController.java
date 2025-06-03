package com.capstone.kakas.crawlingdb.controller;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.capstone.kakas.crawlingdb.service.SeleniumFetchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/selenium")
public class SeleniumController {

    private final SeleniumFetchService fetchService;

    public SeleniumController(SeleniumFetchService fetchService) {
        this.fetchService = fetchService;
    }

    @GetMapping("/danawa")
    public Map<String, Object> parseWithSelenium() {
        String url = "https://prod.danawa.com/info/?pcode=50129522&cate=11338950";

        // 1) Selenium으로 페이지를 로드한 뒤 Jsoup Document로 반환
        Document doc = fetchService.fetchBySelenium(url);

        // 2) <title> 태그 추출
        String title = doc.title();

        // 3) 사이트별 판매가 정보 추출 (쿠팡, G마켓, 11번가, 옥션 등)
        Map<String, String> sitePriceMap = extractSitePriceMapping(doc);

        // 4) 결과 맵에 저장
        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("sitePrices", sitePriceMap);
        result.put("totalSites", sitePriceMap.size());

        // 개별 사이트 가격 정보도 포함
        result.put("쿠팡", sitePriceMap.getOrDefault("쿠팡", "정보 없음"));
        result.put("G마켓", sitePriceMap.getOrDefault("G마켓", "정보 없음"));
        result.put("11번가", sitePriceMap.getOrDefault("11번가", "정보 없음"));
        result.put("옥션", sitePriceMap.getOrDefault("옥션", "정보 없음"));

        return result;
    }

    private Map<String, String> extractSitePriceMapping(Document doc) {
        Map<String, String> sitePriceMap = new HashMap<>();

        try {
            // 다나와 최저가 리스트 테이블에서 행들을 찾기
            Elements tableRows = doc.select("#blog_content .lowest_list table tbody tr");

            System.out.println("찾은 테이블 행 수: " + tableRows.size());

            for (int i = 0; i < tableRows.size(); i++) {
                Element row = tableRows.get(i);

                // 각 행에서 쇼핑몰 이미지/이름 추출
                String siteName = extractSiteName(row, i + 1);

                // 각 행에서 가격 추출
                String price = extractPriceFromRow(row, i + 1);

                if (!siteName.isEmpty() && !price.isEmpty()) {
                    sitePriceMap.put(siteName, price);
                    System.out.println("매핑 성공: " + siteName + " -> " + price);
                }
            }

            // 추가 선택자로 시도 (혹시 구조가 다를 경우)
            if (sitePriceMap.isEmpty()) {
                sitePriceMap = extractSitePriceAlternative(doc);
            }

        } catch (Exception e) {
            System.err.println("사이트-가격 매핑 추출 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return sitePriceMap;
    }

    private String extractSiteName(Element row, int rowIndex) {
        String siteName = "";

        try {
            // 방법 1: img alt 속성에서 사이트명 추출
            Element imgElement = row.select("td.mall img").first();
            if (imgElement != null) {
                siteName = imgElement.attr("alt");
            }

            // 방법 2: 링크 텍스트에서 사이트명 추출
            if (siteName.isEmpty()) {
                Element linkElement = row.select("td.mall a").first();
                if (linkElement != null) {
                    siteName = linkElement.text().trim();
                }
            }

            // 방법 3: CSS 선택자로 직접 지정 (제공해주신 선택자 활용)
            if (siteName.isEmpty()) {
                String selector = String.format("#blog_content .lowest_list table tbody tr:nth-child(%d) td.mall img", rowIndex);
                Element directImg = row.ownerDocument().selectFirst(selector);
                if (directImg != null) {
                    siteName = directImg.attr("alt");
                }
            }

            // 사이트명 정규화 (공통 사이트명으로 변환)
            siteName = normalizeSiteName(siteName);

        } catch (Exception e) {
            System.err.println("사이트명 추출 오류 (행 " + rowIndex + "): " + e.getMessage());
        }

        return siteName;
    }

    private String extractPriceFromRow(Element row, int rowIndex) {
        String price = "";

        try {
            // 방법 1: 제공해주신 선택자 패턴 사용
            Element priceElement = row.select("td.price em.prc_t").first();
            if (priceElement != null) {
                price = priceElement.text().trim();
            }

            // 방법 2: 일반적인 가격 선택자
            if (price.isEmpty()) {
                priceElement = row.select("td.price a span em").first();
                if (priceElement != null) {
                    price = priceElement.text().trim();
                }
            }

            // 방법 3: CSS 선택자로 직접 지정
            if (price.isEmpty()) {
                String selector = String.format("#blog_content .lowest_list table tbody tr:nth-child(%d) td.price em", rowIndex);
                Element directPrice = row.ownerDocument().selectFirst(selector);
                if (directPrice != null) {
                    price = directPrice.text().trim();
                }
            }

            // 방법 4: 다양한 가격 클래스 시도
            if (price.isEmpty()) {
                String[] priceSelectors = {
                        "td.price .prc_t",
                        "td.price em",
                        "td.price span",
                        "td.price strong",
                        ".price em",
                        ".price"
                };

                for (String selector : priceSelectors) {
                    Element elem = row.select(selector).first();
                    if (elem != null && !elem.text().trim().isEmpty()) {
                        price = elem.text().trim();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("가격 추출 오류 (행 " + rowIndex + "): " + e.getMessage());
        }

        return price;
    }

    private String normalizeSiteName(String rawSiteName) {
        if (rawSiteName == null || rawSiteName.trim().isEmpty()) {
            return "";
        }

        String normalized = rawSiteName.trim();

        // 쿠팡 관련
        if (normalized.contains("쿠팡") || normalized.toLowerCase().contains("coupang")) {
            return "쿠팡";
        }
        // G마켓 관련
        else if (normalized.contains("G마켓") || normalized.contains("지마켓") || normalized.toLowerCase().contains("gmarket")) {
            return "G마켓";
        }
        // 11번가 관련
        else if (normalized.contains("11번가") || normalized.contains("십일번가") || normalized.toLowerCase().contains("11st")) {
            return "11번가";
        }
        // 옥션 관련
        else if (normalized.contains("옥션") || normalized.toLowerCase().contains("auction")) {
            return "옥션";
        }
        // 네이버쇼핑 관련
        else if (normalized.contains("네이버") || normalized.toLowerCase().contains("naver")) {
            return "네이버쇼핑";
        }
        // 티몬 관련
        else if (normalized.contains("티몬") || normalized.toLowerCase().contains("tmon")) {
            return "티몬";
        }
        // 위메프 관련
        else if (normalized.contains("위메프") || normalized.toLowerCase().contains("wemakeprice")) {
            return "위메프";
        }

        return normalized;
    }

    private Map<String, String> extractSitePriceAlternative(Document doc) {
        Map<String, String> sitePriceMap = new HashMap<>();

        try {
            // 대안 선택자들 시도
            String[] alternativeSelectors = {
                    ".prod_pricelist tbody tr",
                    ".price_list tbody tr",
                    ".commerce_list .commerce_item",
                    ".shop_list .shop_item"
            };

            for (String selector : alternativeSelectors) {
                Elements rows = doc.select(selector);
                if (!rows.isEmpty()) {
                    System.out.println("대안 선택자 사용: " + selector);

                    for (Element row : rows) {
                        String siteName = extractSiteNameAlternative(row);
                        String price = extractPriceAlternative(row);

                        if (!siteName.isEmpty() && !price.isEmpty()) {
                            sitePriceMap.put(siteName, price);
                        }
                    }

                    if (!sitePriceMap.isEmpty()) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("대안 추출 방법 오류: " + e.getMessage());
        }

        return sitePriceMap;
    }

    private String extractSiteNameAlternative(Element row) {
        Element siteElement = row.selectFirst("img");
        if (siteElement != null) {
            String alt = siteElement.attr("alt");
            if (!alt.isEmpty()) {
                return normalizeSiteName(alt);
            }
        }

        Element linkElement = row.selectFirst("a");
        if (linkElement != null) {
            return normalizeSiteName(linkElement.text());
        }

        return "";
    }

    private String extractPriceAlternative(Element row) {
        String[] priceSelectors = {".prc_t", "em", ".price", "strong", "span"};

        for (String selector : priceSelectors) {
            Element priceElement = row.select(selector).first();
            if (priceElement != null) {
                String priceText = priceElement.text().trim();
                if (priceText.matches(".*\\d.*")) { // 숫자가 포함된 경우만
                    return priceText;
                }
            }
        }

        return "";
    }

    // 디버깅용 엔드포인트 추가
    @GetMapping("/danawa/debug")
    public Map<String, Object> debugDanawa() {
        String url = "https://prod.danawa.com/info/?pcode=50129522&cate=11338950";
        Document doc = fetchService.fetchBySelenium(url);

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("title", doc.title());

        // 테이블 구조 확인
        Elements tableRows = doc.select("#blog_content .lowest_list table tbody tr");
        debugInfo.put("tableRowCount", tableRows.size());

        List<Map<String, String>> rowDetails = new ArrayList<>();
        for (int i = 0; i < Math.min(tableRows.size(), 5); i++) { // 최대 5개 행만 디버깅
            Element row = tableRows.get(i);
            Map<String, String> rowInfo = new HashMap<>();

            // 각 행의 HTML 구조 확인
            rowInfo.put("rowIndex", String.valueOf(i + 1));
            rowInfo.put("html", row.html().substring(0, Math.min(row.html().length(), 300)));

            // 이미지 요소 확인
            Element imgElement = row.select("td.mall img").first();
            if (imgElement != null) {
                rowInfo.put("imgSrc", imgElement.attr("src"));
                rowInfo.put("imgAlt", imgElement.attr("alt"));
            }

            // 가격 요소 확인
            Element priceElement = row.select("td.price em.prc_t").first();
            if (priceElement != null) {
                rowInfo.put("price", priceElement.text());
            } else {
                // 다른 가격 선택자들 시도
                Elements allPriceElements = row.select("td.price *");
                List<String> priceTexts = new ArrayList<>();
                for (Element elem : allPriceElements) {
                    if (!elem.text().trim().isEmpty()) {
                        priceTexts.add(elem.tagName() + ":" + elem.text().trim());
                    }
                }
                rowInfo.put("allPriceElements", String.join(", ", priceTexts));
            }

            rowDetails.add(rowInfo);
        }

        debugInfo.put("rowDetails", rowDetails);

        // 전체 HTML에서 가격 관련 요소들 찾기
        Elements allPriceElements = doc.select("[class*=prc], [class*=price], em, strong");
        List<String> priceElementInfo = new ArrayList<>();
        for (int i = 0; i < Math.min(allPriceElements.size(), 10); i++) {
            Element elem = allPriceElements.get(i);
            if (elem.text().matches(".*\\d.*")) { // 숫자가 포함된 경우만
                priceElementInfo.add(elem.className() + " -> " + elem.text().trim());
            }
        }
        debugInfo.put("priceElements", priceElementInfo);

        return debugInfo;
    }
}