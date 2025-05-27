package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.SiteSearchOverride;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteSearchOverrideRepository extends JpaRepository<SiteSearchOverride, Long> {
    List<SiteSearchOverride> findBySiteNameAndModelCodeIdOrderByPriorityAsc(SiteName siteName, Long modelCodeId);
}
