package com.capstone.kakas.devdb.repository;

import com.capstone.kakas.devdb.domain.ChatAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAnalysisRepository extends JpaRepository<ChatAnalysis, Long> {
}
