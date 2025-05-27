package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
