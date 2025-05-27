package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.ExcludeKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ExcludeKeywordRepository extends JpaRepository<ExcludeKeyword, Long> {
    List<ExcludeKeyword> findByModelCodeId(Long modelCodeId);
}
