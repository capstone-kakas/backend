package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.ExcludeKeyword;
import com.capstone.kakas.crawlingdb.domain.ModelCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExcludeKeywordRepository extends JpaRepository<ExcludeKeyword, Long> {
    List<ExcludeKeyword> findByModelCodeId(Long modelCodeId);

    List<ExcludeKeyword> findByModelCode(ModelCode modelCode);
}
