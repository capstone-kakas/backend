package com.capstone.kakas.crawlingdb.controller;

import com.capstone.kakas.crawlingdb.service.SeleniumFetchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.jsoup.nodes.Document;
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
        Document doc = fetchService.fetchBySelenium(url);
        // 예: 본체 번들 가격 정보 꺼내기
        String price = doc.selectFirst("span.prod-price__price").text();
        return Map.of(
                "title", doc.title(),
                "price", price
        );
    }
}
