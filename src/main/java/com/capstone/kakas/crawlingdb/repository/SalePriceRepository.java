package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.SalePrice;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {
    @Query("SELECT AVG(sp.price) FROM SalePrice sp " +
            "WHERE sp.modelCode.id = :modelCodeId " +
            "  AND sp.site.name = :siteName " +
            "  AND sp.crawledAt BETWEEN :start AND :end")
    BigDecimal findAveragePrice(
            @Param("modelCodeId") Long modelCodeId,
            @Param("siteName") SiteName siteName,
            @Param("start") LocalDateTime start,
            @Param("end")          LocalDateTime end);

    List<SalePrice> findByModelCodeIdAndSiteNameAndCrawledAtBetween(
            Long modelCodeId, SiteName siteName, LocalDateTime start, LocalDateTime end);
}
