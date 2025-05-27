package com.capstone.kakas.crawlingdb.repository;


import com.capstone.kakas.crawlingdb.domain.SiteModelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteModelMappingRepository extends JpaRepository<SiteModelMapping, Long> {
    List<SiteModelMapping> findByModelCodeId(Long modelCodeId);
}
