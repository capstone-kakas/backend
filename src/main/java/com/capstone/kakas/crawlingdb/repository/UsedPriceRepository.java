package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsedPriceRepository extends JpaRepository<UsedPrice, Long> {
    List<UsedPrice> findByModelCodeIdAndSiteNameAndCrawledAtBetween(
            Long modelCodeId, SiteName siteName, LocalDateTime start, LocalDateTime end);
}
