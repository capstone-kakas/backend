package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByProductId(Long productId);
}
