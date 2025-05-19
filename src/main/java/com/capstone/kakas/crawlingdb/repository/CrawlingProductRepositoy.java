package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.CrawlingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlingProductRepositoy extends JpaRepository<CrawlingProduct, Long> {
}
