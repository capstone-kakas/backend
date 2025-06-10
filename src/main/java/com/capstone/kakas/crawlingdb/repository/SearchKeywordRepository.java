package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
    List<SearchKeyword> findAllByProductId(Long productId);
}
