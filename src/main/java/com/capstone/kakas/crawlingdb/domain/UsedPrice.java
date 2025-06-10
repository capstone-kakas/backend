package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "used_price")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedPrice extends BaseEntity {
    /**
     * 중고상품 가격 저장 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int price;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreatedDate
    private LocalDateTime crawledAt;

    public void setPrice(int price) {
        this.price = price;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

