package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SalePrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer price;

    private String site;

    private String siteUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawling_product_id")
    private CrawlingProduct crawlingProduct;
    
}
