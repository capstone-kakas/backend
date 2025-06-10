package com.capstone.kakas.devdb.dto.request;

import com.capstone.kakas.devdb.domain.ChatRoom;
import lombok.*;

public class AiRequestDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class recommendRequestDto {
        private String chatTitle;
        private String chatContent;
        private String price;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class aiRequestDto {
        private ChatRoom chatRoom;
        private String message;
    }
}
