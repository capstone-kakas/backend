package com.capstone.kakas.devdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private String seller;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private List<ChatMessage> chatMessages = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 연관관계 편의 메서드
    public void setProduct(Product product) {
        this.product = product;
    }

    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
        message.setChatRoom(this);
    }
}
