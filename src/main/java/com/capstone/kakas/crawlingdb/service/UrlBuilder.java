package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.Site;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * URL Builder: 사이트별 인코딩 및 URL 템플릿 치환
 */
@Service
@RequiredArgsConstructor
public class UrlBuilder {
    public String buildSearchUrl(Site site, String rawQuery, int page) {
        String encoded = encode(rawQuery, SiteName.valueOf(site.getName()));
        return site.getSearchUrlTemplate()
                .replace("{query}", encoded)
                .replace("{page}", String.valueOf(page));
    }

    private String encode(String query, SiteName siteName) {
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
