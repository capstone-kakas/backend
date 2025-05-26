package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {
    /**
     * 검색 로그 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_code_id", nullable = false)
    private ModelCode modelCode;

    @Column(nullable = false, length = 255)
    private String query;

    @Column(nullable = false)
    private Boolean success;

    @Column(nullable = false)
    private Integer latencyMs;

    @Column(nullable = false)
    private LocalDateTime loggedAt;
}

