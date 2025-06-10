package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.ExcludeKeyword;
import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.ProductAlias;
import com.capstone.kakas.crawlingdb.domain.SearchKeyword;
import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
import com.capstone.kakas.crawlingdb.dto.FilteredResultDto;
import com.capstone.kakas.crawlingdb.repository.ExcludeKeywordRepository;
import com.capstone.kakas.crawlingdb.repository.ProductAliasRepository;
import com.capstone.kakas.crawlingdb.repository.ProductRepository;
import com.capstone.kakas.crawlingdb.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TitleFilteringService {

    private final ProductRepository productRepository;
    private final ExcludeKeywordRepository excludeKeywordRepository;
    private final SearchKeywordRepository searchKeywordRepository;
    private final ProductAliasRepository productAliasRepository;




    /**
     * 1. ExcludeKeyword 필터링 - 제외 키워드가 포함된 항목 제거
     */
    public List<CrawlingResultDto> filteringExcludeKeyword(List<CrawlingResultDto> crawlingResults) {
        log.info("=== ExcludeKeyword 필터링 시작 ===");
        log.info("필터링 전 총 항목 수: {}", crawlingResults.size());

        List<CrawlingResultDto> filteredResults = new ArrayList<>();
        int excludedCount = 0;

        for (CrawlingResultDto result : crawlingResults) {
            try {
                // Product 조회
                Optional<Product> productOpt = productRepository.findByName(result.getProductName());
                if (productOpt.isEmpty()) {
                    log.warn("Product를 찾을 수 없음: {}", result.getProductName());
                    continue;
                }

                Product product = productOpt.get();
                List<ExcludeKeyword> excludeKeywords = excludeKeywordRepository.findAllByProductId(product.getId());

                boolean shouldExclude = false;
                String matchedKeyword = null;

                // ExcludeKeyword 검사
                for (ExcludeKeyword excludeKeyword : excludeKeywords) {
                    String keyword = excludeKeyword.getKeyword().toLowerCase();
                    String saleTitle = result.getSaleTitle().toLowerCase();

                    if (saleTitle.contains(keyword)) {
                        shouldExclude = true;
                        matchedKeyword = excludeKeyword.getKeyword();
                        break;
                    }
                }

                if (shouldExclude) {
                    log.info("❌ EXCLUDED - Product: '{}', Title: '{}', Matched Keyword: '{}'",
                            result.getProductName(), result.getSaleTitle(), matchedKeyword);
                    excludedCount++;
                } else {
                    log.debug("✅ PASSED - Product: '{}', Title: '{}'",
                            result.getProductName(), result.getSaleTitle());
                    filteredResults.add(result);
                }

            } catch (Exception e) {
                log.error("ExcludeKeyword 필터링 중 오류 발생 - Product: '{}', Title: '{}', Error: {}",
                        result.getProductName(), result.getSaleTitle(), e.getMessage());
                // 오류 발생 시 안전하게 포함
                filteredResults.add(result);
            }
        }

        log.info("=== ExcludeKeyword 필터링 완료 ===");
        log.info("제외된 항목 수: {}", excludedCount);
        log.info("필터링 후 항목 수: {}", filteredResults.size());
        log.info("필터링 비율: {:.2f}%",
                crawlingResults.size() > 0 ? (double) excludedCount / crawlingResults.size() * 100 : 0);

        return filteredResults;
    }


    /**
     * 2. IncludeKeyword 필터링 - 포함 키워드가 있는 Product만 해당 키워드 검사
     */
    public List<CrawlingResultDto> filteringIncludeKeyword(List<CrawlingResultDto> crawlingResults) {
        log.info("=== IncludeKeyword 필터링 시작 ===");
        log.info("필터링 전 총 항목 수: {}", crawlingResults.size());

        List<CrawlingResultDto> filteredResults = new ArrayList<>();
        int excludedCount = 0;
        int noIncludeKeywordCount = 0;

        for (CrawlingResultDto result : crawlingResults) {
            try {
                // Product 조회
                Optional<Product> productOpt = productRepository.findByName(result.getProductName());
                if (productOpt.isEmpty()) {
                    log.warn("Product를 찾을 수 없음: {}", result.getProductName());
                    continue;
                }

                Product product = productOpt.get();
                List<SearchKeyword> includeKeywords = searchKeywordRepository.findAllByProductId(product.getId());

                // IncludeKeyword가 없으면 자동 통과
                if (includeKeywords == null || includeKeywords.isEmpty()) {
                    log.debug("✅ AUTO PASS - Product: '{}', Title: '{}' (IncludeKeyword 없음)",
                            result.getProductName(), result.getSaleTitle());
                    filteredResults.add(result);
                    noIncludeKeywordCount++;
                    continue;
                }

                boolean shouldInclude = false;
                String matchedKeyword = null;

                // IncludeKeyword 검사
                for (SearchKeyword includeKeyword : includeKeywords) {
                    String keyword = includeKeyword.getKeyword().toLowerCase();
                    String saleTitle = result.getSaleTitle().toLowerCase();

                    if (saleTitle.contains(keyword)) {
                        shouldInclude = true;
                        matchedKeyword = includeKeyword.getKeyword();
                        break;
                    }
                }

                if (shouldInclude) {
                    log.info("✅ INCLUDED - Product: '{}', Title: '{}', Matched Keyword: '{}'",
                            result.getProductName(), result.getSaleTitle(), matchedKeyword);
                    filteredResults.add(result);
                } else {
                    log.info("❌ EXCLUDED - Product: '{}', Title: '{}', Required Keywords: [{}]",
                            result.getProductName(), result.getSaleTitle(),
                            includeKeywords.stream()
                                    .map(SearchKeyword::getKeyword)
                                    .collect(Collectors.joining(", ")));
                    excludedCount++;
                }

            } catch (Exception e) {
                log.error("IncludeKeyword 필터링 중 오류 발생 - Product: '{}', Title: '{}', Error: {}",
                        result.getProductName(), result.getSaleTitle(), e.getMessage());
                // 오류 발생 시 안전하게 포함
                filteredResults.add(result);
            }
        }

        log.info("=== IncludeKeyword 필터링 완료 ===");
        log.info("자동 통과 항목 수 (IncludeKeyword 없음): {}", noIncludeKeywordCount);
        log.info("제외된 항목 수: {}", excludedCount);
        log.info("필터링 후 항목 수: {}", filteredResults.size());
        log.info("필터링 비율: {:.2f}%",
                crawlingResults.size() > 0 ? (double) excludedCount / crawlingResults.size() * 100 : 0);

        return filteredResults;
    }



    /**
     * 3. ProductAlias 치환 - saleTitle에서 alias를 replacement로 변경
     */
    public List<CrawlingResultDto> replaceAlias(List<CrawlingResultDto> crawlingResults) {
        log.info("=== ProductAlias 치환 시작 ===");
        log.info("치환 대상 항목 수: {}", crawlingResults.size());

        List<CrawlingResultDto> replacedResults = new ArrayList<>();
        int totalReplacements = 0;

        for (CrawlingResultDto result : crawlingResults) {
            try {
                // Product 조회
                Optional<Product> productOpt = productRepository.findByName(result.getProductName());
                if (productOpt.isEmpty()) {
                    log.warn("Product를 찾을 수 없음: {}", result.getProductName());
                    replacedResults.add(result);
                    continue;
                }

                Product product = productOpt.get();
                List<ProductAlias> productAliases = productAliasRepository.findAllByProductId(product.getId());

                String originalTitle = result.getSaleTitle();
                String modifiedTitle = originalTitle;
                List<String> appliedReplacements = new ArrayList<>();

                // ProductAlias 치환 수행
                for (ProductAlias alias : productAliases) {
                    String aliasKeyword = alias.getAlias();
                    String replacement = alias.getReplacement();

                    // 대소문자 구분 없이 치환 (단어 단위로 치환하여 부분 문자열 오치환 방지)
                    String regex = "(?i)\\b" + Pattern.quote(aliasKeyword) + "\\b";
                    String beforeReplace = modifiedTitle;
                    modifiedTitle = modifiedTitle.replaceAll(regex, replacement);

                    if (!beforeReplace.equals(modifiedTitle)) {
                        appliedReplacements.add(String.format("'%s' → '%s'", aliasKeyword, replacement));
                        totalReplacements++;
                    }
                }

                // 결과 생성 (제목이 변경된 경우 새로운 DTO 생성)
                CrawlingResultDto modifiedResult;
                if (!originalTitle.equals(modifiedTitle)) {
                    modifiedResult = CrawlingResultDto.builder()
                            .productId(result.getProductId())
                            .productName(result.getProductName())
                            .price(result.getPrice())
                            .SaleTitle(modifiedTitle)
//                            .url(result.getUrl())
                            .build();

                    log.info("🔄 REPLACED - Product: '{}', Original: '{}', Modified: '{}', Applied: [{}]",
                            result.getProductName(), originalTitle, modifiedTitle,
                            String.join(", ", appliedReplacements));
                } else {
                    modifiedResult = result;
                    log.debug("⚪ NO CHANGE - Product: '{}', Title: '{}'",
                            result.getProductName(), originalTitle);
                }

                replacedResults.add(modifiedResult);

            } catch (Exception e) {
                log.error("ProductAlias 치환 중 오류 발생 - Product: '{}', Title: '{}', Error: {}",
                        result.getProductName(), result.getSaleTitle(), e.getMessage());
                // 오류 발생 시 원본 그대로 포함
                replacedResults.add(result);
            }
        }

        log.info("=== ProductAlias 치환 완료 ===");
        log.info("총 치환 횟수: {}", totalReplacements);
        log.info("치환된 항목 수: {}", replacedResults.stream()
                .mapToInt(r -> r.getSaleTitle().equals(
                        crawlingResults.stream()
                                .filter(orig -> orig.getProductId().equals(r.getProductId()))
                                .findFirst()
                                .map(CrawlingResultDto::getSaleTitle)
                                .orElse(r.getSaleTitle())) ? 0 : 1)
                .sum());
        log.info("최종 항목 수: {}", replacedResults.size());

        return replacedResults;
    }













//    * 4. 코사인 유사도 필터링 (추후 구현 예정)
//     */
    public List<CrawlingResultDto> cosineSimilarityFiltering(List<CrawlingResultDto> crawlingResults) {
        log.info("=== 코사인 유사도 필터링 시작 ===");
        log.info("입력 항목 수: {}", crawlingResults.size());

        // TODO: 유사도 비교 로직 구현 예정
        log.info("⚠️ 코사인 유사도 필터링은 아직 구현되지 않았습니다. 모든 항목을 통과시킵니다.");

        log.info("=== 코사인 유사도 필터링 완료 ===");
        log.info("출력 항목 수: {}", crawlingResults.size());

        return crawlingResults;
    }



}
