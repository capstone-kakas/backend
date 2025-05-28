package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Site;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.stereotype.Component;


@Component
public class UrlBuilder {

    /**
     * rawQuery 와 site.getSiteName() 을 받아서 encodeQuery(...) 를 호출합니다.
     */
    public String buildSearchUrl(String rawQuery, Site site) {
        // 두 인자를 받는 메서드가 분명히 있어야 합니다
        String encoded = encodeQuery(rawQuery, site.getSiteName());
        return site.getSearchUrlTemplate().replace("{query}", encoded);
    }

    private String encodeQuery(String query, SiteName siteName) {
        switch (siteName) {
            case GMARKET:
                // UTF-8 encode, spaces as '+'
                return java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8)
                        .replace("%20", "+");
            case ELEVENSTREET:
                // Double encode to represent '%' as '%25'
                String first = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
                return java.net.URLEncoder.encode(first, java.nio.charset.StandardCharsets.UTF_8);
            case COUPANG:
                // Standard UTF-8 encode, spaces as '%20'
                return java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            case BUNGAE:
                // Use raw query with spaces encoded by browser
                return query;
            default:
                return java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
