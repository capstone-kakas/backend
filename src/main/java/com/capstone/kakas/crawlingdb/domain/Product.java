package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Product {
    /**
     * 최상위 제품 엔티티 (예: PlayStation 5)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 255)
    private String saleUrl;

    @Column(name = "bunjang_url", length = 255)
    private String bunjangUrl;

    @Column(name = "title_selector", columnDefinition = "TEXT")
    private String titleSelector;

    @Column(name = "price_selector", columnDefinition = "TEXT")
    private String priceSelector;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_price_id")
    private List<UsedPrice> usedPrices = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_price_id")
    private List<SalePrice> salePrices = new ArrayList<>();
}
