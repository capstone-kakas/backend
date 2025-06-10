package com.capstone.kakas.crawlingdb.service;

import com.capstone.kakas.crawlingdb.domain.*;
import com.capstone.kakas.crawlingdb.dto.CrawlingResultDto;
import com.capstone.kakas.crawlingdb.dto.FilteredResultDto;
import com.capstone.kakas.crawlingdb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.text.similarity.CosineDistance;

import java.util.*;
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
    private final UsedPriceRepository usedPriceRepository;

    // 코사인 유사도 임계값 (0.0 ~ 1.0, 낮을수록 더 유사함)
    private static final double COSINE_SIMILARITY_THRESHOLD = 0.3; // 70% 이상 유사도


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

    if (crawlingResults == null || crawlingResults.isEmpty()) {
        log.warn("입력 데이터가 비어있습니다.");
        return new ArrayList<>();
    }

    List<CrawlingResultDto> validResults = new ArrayList<>();

    for (CrawlingResultDto item : crawlingResults) {
        String productName = item.getProductName();
        String saleTitle = item.getSaleTitle();

        if (productName == null || saleTitle == null) {
            log.warn("제품명 또는 판매제목이 null입니다. 제외됩니다: {}", item);
            continue;
        }

        // 전처리된 텍스트로 코사인 유사도 계산
        String preprocessedProductName = preprocessText(productName);
        String preprocessedSaleTitle = preprocessText(saleTitle);

        double similarity = calculateCosineSimilarity(preprocessedProductName, preprocessedSaleTitle);

        log.info("유사도 검사 (유사도: {}): '{}' vs '{}'",
                similarity * 100, productName, saleTitle);
        log.debug("전처리 결과: '{}' vs '{}'", preprocessedProductName, preprocessedSaleTitle);

        if (similarity >= (1.0 - COSINE_SIMILARITY_THRESHOLD)) { // 임계값을 유사도 기준으로 변경
            validResults.add(item);
            log.info("✓ 유사도 조건 만족 - 평균가격 계산에 포함");
        } else {
            log.info("✗ 유사도 조건 불만족 - 평균가격 계산에서 제외");
        }
    }

    log.info("=== 코사인 유사도 필터링 완료 ===");
    log.info("유효 항목 수: {} / 전체 항목 수: {} (제외된 항목: {})",
            validResults.size(), crawlingResults.size(), crawlingResults.size() - validResults.size());

    // 유효한 결과로 평균 가격 계산 및 저장
    saveAveragePrices(validResults);

    return validResults;
}

    /**
     * 텍스트 전처리: 공백 제거, 소문자 변환, 특수문자 정리
     */
    private String preprocessText(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replaceAll("\\s+", "") // 모든 공백 제거
                .replaceAll("[^a-zA-Z0-9가-힣]", "") // 영문, 숫자, 한글만 유지
                .trim();
    }

    /**
     * 코사인 유사도 계산 (문자 빈도 기반)
     */
    private double calculateCosineSimilarity(String text1, String text2) {
        if (text1.isEmpty() && text2.isEmpty()) {
            return 1.0;
        }
        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        // 문자 빈도 맵 생성
        Map<Character, Integer> freq1 = getCharacterFrequency(text1);
        Map<Character, Integer> freq2 = getCharacterFrequency(text2);

        // 모든 고유 문자 수집
        Set<Character> allChars = new HashSet<>();
        allChars.addAll(freq1.keySet());
        allChars.addAll(freq2.keySet());

        // 벡터 내적과 크기 계산
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (Character ch : allChars) {
            int count1 = freq1.getOrDefault(ch, 0);
            int count2 = freq2.getOrDefault(ch, 0);

            dotProduct += count1 * count2;
            magnitude1 += count1 * count1;
            magnitude2 += count2 * count2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * 문자 빈도 계산
     */
    private Map<Character, Integer> getCharacterFrequency(String text) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char ch : text.toCharArray()) {
            frequency.put(ch, frequency.getOrDefault(ch, 0) + 1);
        }
        return frequency;
    }

    /**
     * 유효한 결과의 평균 가격을 계산하고 UsedPrice 엔티티에 저장
     */
    @Transactional
    public void saveAveragePrices(List<CrawlingResultDto> validResults) {
        log.info("=== 평균 가격 계산 및 저장 시작 ===");

        // 제품별로 그룹화
        Map<Long, List<CrawlingResultDto>> groupedByProduct = validResults.stream()
                .collect(Collectors.groupingBy(CrawlingResultDto::getProductId));

        for (Map.Entry<Long, List<CrawlingResultDto>> entry : groupedByProduct.entrySet()) {
            Long productId = entry.getKey();
            List<CrawlingResultDto> productResults = entry.getValue();

            try {
                // 가격 파싱 및 평균 계산
                List<Integer> prices = productResults.stream()
                        .map(this::parsePrice)
                        .filter(price -> price != null && price > 0)
                        .collect(Collectors.toList());

                if (prices.isEmpty()) {
                    log.warn("제품 ID {}의 유효한 가격 정보가 없습니다.", productId);
                    continue;
                }

                double averagePrice = prices.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

                int avgPrice = (int) Math.round(averagePrice);

                log.info("제품 ID {}: {} 개 가격 샘플, 평균 가격: {}원",
                        productId, prices.size(), String.format("%,d", avgPrice));

                // Product 엔티티 조회
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    // UsedPrice 엔티티 생성 및 저장
                    UsedPrice usedPrice = new UsedPrice();
                    usedPrice.setPrice(avgPrice);
                    usedPrice.setProduct(product);

                    usedPriceRepository.save(usedPrice);

                    log.info("제품 '{}' (ID: {})의 평균 가격 {}원이 저장되었습니다.",
                            product.getName(), productId, String.format("%,d", avgPrice));
                } else {
                    log.error("제품 ID {}를 찾을 수 없습니다.", productId);
                }

            } catch (Exception e) {
                log.error("제품 ID {}의 평균 가격 저장 중 오류 발생: {}", productId, e.getMessage(), e);
            }
        }

        log.info("=== 평균 가격 계산 및 저장 완료 ===");
    }

    /**
     * 가격 문자열을 정수로 파싱
     */
    private Integer parsePrice(CrawlingResultDto dto) {
        if (dto.getPrice() == null || dto.getPrice().trim().isEmpty()) {
            return null;
        }

        try {
            // 가격 문자열에서 숫자만 추출
            String priceStr = dto.getPrice()
                    .replaceAll("[^0-9]", "") // 숫자가 아닌 모든 문자 제거
                    .trim();

            if (priceStr.isEmpty()) {
                return null;
            }

            return Integer.parseInt(priceStr);

        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: '{}' (제품: {})", dto.getPrice(), dto.getProductName());
            return null;
        }
    }
}
