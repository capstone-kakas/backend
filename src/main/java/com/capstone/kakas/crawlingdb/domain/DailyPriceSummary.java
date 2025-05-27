package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_price_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"model_code_id","site_name","date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPriceSummary {
    /**
     * 일별 가격 요약 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_code_id", nullable = false)
    private ModelCode modelCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_name", nullable = false)
    private Site site;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal avgPrice;
}
