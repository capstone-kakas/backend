package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "used_price")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedPrice {
    /**
     * 중고상품 가격 저장 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 100)
    private String sellerId;

    @Column(length = 50)
    private String condition;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime crawledAt;
}

