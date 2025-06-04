package com.capstone.kakas.devdb.repository;

import com.capstone.kakas.devdb.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DEVProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

}
