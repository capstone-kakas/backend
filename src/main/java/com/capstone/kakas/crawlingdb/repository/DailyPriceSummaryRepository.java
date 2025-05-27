package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.DailyPriceSummary;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyPriceSummaryRepository extends JpaRepository<DailyPriceSummary, Long> {
    Optional<DailyPriceSummary> findByModelCodeIdAndSiteNameAndDate(
            Long modelCodeId, SiteName siteName, LocalDate date);
}
