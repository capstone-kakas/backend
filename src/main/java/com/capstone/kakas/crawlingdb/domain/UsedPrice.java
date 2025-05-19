package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UsedPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal price;

    private Integer sampleSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawling_product_id")
    private CrawlingProduct crawlingProduct;
}
