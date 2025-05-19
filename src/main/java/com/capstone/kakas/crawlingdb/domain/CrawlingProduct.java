package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CrawlingProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    @OneToMany(mappedBy = "crawlingProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalePrice> salePrices = new ArrayList<>();

    @OneToMany(mappedBy = "crawlingProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedPrice> usedPrices = new ArrayList<>();
}
