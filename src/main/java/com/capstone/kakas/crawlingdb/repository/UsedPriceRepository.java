package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Product;
import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UsedPriceRepository extends JpaRepository<UsedPrice, Long> {
    Optional<UsedPrice> findTopByProductOrderByCreatedAtDesc(Product product);

    List<UsedPrice> findAllByProduct(Product product);
}
