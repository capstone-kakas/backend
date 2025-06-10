package com.capstone.kakas.devdb.domain;

import com.capstone.kakas.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_analysis_id")
    private List<ChatAnalysis> chatAnalyses = new ArrayList<>();


    //연관관계 편의메서드
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    // 연관관계 편의 메서드 - null 체크 추가
    public void addChatAnalysis(ChatAnalysis chatAnalysis) {
        if (this.chatAnalyses == null) {
            this.chatAnalyses = new ArrayList<>();
        }
        this.chatAnalyses.add(chatAnalysis);
        chatAnalysis.setChatMessage(this);
    }

    // Getter에서도 null 체크
    public List<ChatAnalysis> getChatAnalyses() {
        if (this.chatAnalyses == null) {
            this.chatAnalyses = new ArrayList<>();
        }
        return this.chatAnalyses;
    }

}
