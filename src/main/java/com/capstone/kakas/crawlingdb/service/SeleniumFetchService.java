package com.capstone.kakas.crawlingdb.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import org.jsoup.nodes.Document;
@Service
@RequiredArgsConstructor
public class SeleniumFetchService {

    private final WebDriver driver;



    /**
     * Selenium으로 페이지 로드 후
     * Jsoup로 파싱할 수 있는 HTML 문자열을 반환합니다.
     */
    public Document fetchBySelenium(String url) {
        driver.get(url);
        String html = driver.getPageSource();
        return Jsoup.parse(html);
    }
}