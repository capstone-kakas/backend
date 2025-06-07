package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.UsedPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UsedPriceRepository extends JpaRepository<UsedPrice, Long> {

}
