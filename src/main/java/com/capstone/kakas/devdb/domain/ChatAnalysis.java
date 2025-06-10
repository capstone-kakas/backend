package com.capstone.kakas.devdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_analysis")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAnalysis extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String analysis;

    @ManyToOne
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;


    //연관관계 편의 메서드
    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
