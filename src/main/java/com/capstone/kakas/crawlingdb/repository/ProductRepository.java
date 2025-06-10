package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

    /**
     * 번개장터 URL이 설정된 상품 개수 조회
     */
    long countByBunjangUrlIsNotNull();

    /**
     * 번개장터 URL이 설정된 상품 목록 조회
     */
    List<Product> findByBunjangUrlIsNotNull();
}
