package com.capstone.kakas.crawlingdb.service;


import com.capstone.kakas.crawlingdb.domain.SalePrice;
import com.capstone.kakas.crawlingdb.domain.SiteModelMapping;
import com.capstone.kakas.crawlingdb.repository.SalePriceRepository;
import com.capstone.kakas.crawlingdb.repository.SiteModelMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OfficialCrawlService {
    private final SiteModelMappingRepository mappingRepo;
    private final CrawlingService crawlingService;
    private final SalePriceRepository salePriceRepo;

    /**
     * 모든 (site, model) 매핑에 대해 첫 매칭 가격을 가져와 저장
     */
    @Transactional
    public void crawlAndSaveAllOfficial() {
        List<SiteModelMapping> mappings = mappingRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (SiteModelMapping m : mappings) {
            crawlingService
                    .fetchFirstMatchingPrice(m.getModelCode(), m.getSite())
                    .ifPresent(price -> {
                        SalePrice sp = SalePrice.builder()
                                .site(m.getSite())
                                .modelCode(m.getModelCode())
                                .price(price)
                                .crawledAt(now)
                                .build();
                        salePriceRepo.save(sp);
                    });
        }
    }
}
