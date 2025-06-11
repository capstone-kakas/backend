package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.SalePrice;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {
    Optional<SalePrice> findTopByProductOrderByCreatedAtDesc(Product product);
}
