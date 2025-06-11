package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "sale_price")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePrice extends BaseEntity {
    /**
     * 새상품 가격 저장 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    private SiteName siteName;

    public void setPrice(int price) {
        this.price = price;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
