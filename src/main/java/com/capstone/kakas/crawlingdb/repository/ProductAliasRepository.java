package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.ProductAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAliasRepository extends JpaRepository<ProductAlias, Long> {
}
