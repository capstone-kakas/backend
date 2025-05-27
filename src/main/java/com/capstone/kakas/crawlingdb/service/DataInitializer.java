package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Site;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import com.capstone.kakas.crawlingdb.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataInitializer {

    private final SiteRepository siteRepo;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            for (SiteName siteName : SiteName.values()) {
                siteRepo.findBySiteName(siteName)
                        .orElseGet(() -> siteRepo.save(
                                Site.builder()
                                        .name(siteName.name())
                                        .siteName(siteName)
                                        .searchUrlTemplate(defaultTemplate(siteName))
                                        .build()
                        ));
            }
        } catch (DataAccessException ex) {
            // DB 연결이 안 될 때 발생. 애플리케이션은 계속 띄워둡니다.
            System.err.println("[WARN] DataInitializer - 초기 데이터 주입 실패, DB 연결 확인 필요");
            ex.printStackTrace();
        }
    }

    private String defaultTemplate(SiteName name) {
        switch (name) {
            case GMARKET:
                return "https://www.gmarket.co.kr/n/search?keyword={query}";
            case ELEVENSTREET:
                return "https://search.11st.co.kr/pc/total-search?kwd={query}&tabId=TOTAL_SEARCH";
            case COUPANG:
                return "https://www.coupang.com/np/search?q={query}";
            case BUNGAE:
                return "https://m.bunjang.co.kr/search/products?q={query}";
            default:
                throw new IllegalArgumentException("Unsupported site: " + name);
        }
    }
}