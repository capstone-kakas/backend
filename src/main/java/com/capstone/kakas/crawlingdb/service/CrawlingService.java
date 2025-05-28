package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Site;
import com.capstone.kakas.crawlingdb.domain.ModelCode;
import com.capstone.kakas.crawlingdb.domain.SiteScraperRule;
import com.capstone.kakas.crawlingdb.domain.SearchKeyword;
import com.capstone.kakas.crawlingdb.domain.ExcludeKeyword;
import com.capstone.kakas.crawlingdb.dto.response.CrawledItemDTO;
import com.capstone.kakas.crawlingdb.repository.SiteScraperRuleRepository;
import com.capstone.kakas.crawlingdb.repository.SearchKeywordRepository;
import com.capstone.kakas.crawlingdb.repository.ExcludeKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final UrlBuilder urlBuilder;
    private final SiteScraperRuleRepository ruleRepo;
    private final SearchKeywordRepository searchKeywordRepo;
    private final ExcludeKeywordRepository excludeKeywordRepo;
    private final RestTemplate restTemplate = new RestTemplate();


    private String buildSearchTerm(ModelCode code) {
        // 1) 제품 이름 + 변형 이름
        String term = code.getVariant().getProduct().getName()
                + " " + code.getVariant().getName();

        // 2) 옵션이 있으면 붙이기 (예: 색상, 패키지 구성 등)
        if (code.getOption() != null) {
            term += " " + code.getOption().getName();
        }

        // 3) DB에 저장된 include 키워드 추가 (예: 특별판 이름 등)
        List<String> includes = searchKeywordRepo.findByModelCode(code).stream()
                .map(SearchKeyword::getKeyword)
                .toList();
        if (!includes.isEmpty()) {
            term += " " + String.join(" ", includes);
        }

        return term.trim();
    }
    /**
     * 신상품 첫 매칭 가격 조회
     */
    public Optional<BigDecimal> fetchFirstMatchingPrice(ModelCode code, Site site) {
        String url = urlBuilder.buildSearchUrl(code.getCode(), site);
        List<CrawledItemDTO> items = fetchCrawledItems(url, site);

        return items.stream()
                .filter(item -> isMatch(item.getTitle(), code))
                .map(CrawledItemDTO::getPrice)
                .findFirst();
    }

    /**
     * 제목(title)이 해당 ModelCode 의 include/exclude 키워드 조건을 만족하는지 여부
     */
    private boolean isMatch(String title, ModelCode code) {
        String lcTitle = title.toLowerCase();

        // 제외 키워드 하나라도 포함되면 제외
        List<String> excludes = excludeKeywordRepo.findByModelCode(code).stream()
                .map(ExcludeKeyword::getKeyword)
                .map(String::toLowerCase)
                .toList();
        if (excludes.stream().anyMatch(lcTitle::contains)) {
            return false;
        }

        // 포함 키워드가 하나도 없다면 모두 허용
        List<String> includes = searchKeywordRepo.findByModelCode(code).stream()
                .map(SearchKeyword::getKeyword)
                .map(String::toLowerCase)
                .toList();
        if (includes.isEmpty()) {
            return true;
        }

        // 모든 포함 키워드를 만족해야 허용
        return includes.stream().allMatch(lcTitle::contains);
    }

    /**
     * 공통: URL 호출 → CSS 선택자 기반 CrawledItemDTO 리스트 추출
     */
    private List<CrawledItemDTO> fetchCrawledItems(String url, Site site) {
        try {
            String html = restTemplate.getForObject(url, String.class);
            Document doc = Jsoup.parse(html);

            // DB에 정의된 CSS 룰 로딩
            Map<String, String> selectors = ruleRepo.findBySite(site).stream()
                    .collect(Collectors.toMap(
                            SiteScraperRule::getFieldName,
                            SiteScraperRule::getCssSelector
                    ));

            // 컨테이너 셀렉터
            String containerSel = selectors.get("item");
            Elements containers = doc.select(containerSel);
            if (containers.isEmpty()) {
                return Collections.emptyList();
            }

            // 첫 번째 컨테이너 처리
            Element el = containers.get(0);

            // title과 price 셀렉터
            String titleCss = selectors.get("title");
            String priceCss = selectors.get("price");

            String titleText = el.select(titleCss).text();
            String priceText = el.select(priceCss).text().replaceAll("[^0-9]", "");
            BigDecimal price = new BigDecimal(priceText);

            return List.of(
                    CrawledItemDTO.builder()
                            .title(titleText)
                            .price(price)
                            .build()
            );
        } catch (Exception ex) {
            // TODO: 로깅 추가
            return Collections.emptyList();
        }
    }
}
