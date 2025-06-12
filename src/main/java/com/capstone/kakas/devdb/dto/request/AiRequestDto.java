package com.capstone.kakas.devdb.dto.request;

import com.capstone.kakas.devdb.domain.ChatRoom;
import lombok.*;

import java.util.List;

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
        private List<String> chat;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class messageAnalysisRequestDto {
        private String chatTitle;
        private String chatContent;
        private String price;
        private String status;
        private List<List<String>> chat;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class chatSellerDto {
        private String question;
        private String seller_chat;
        private String chatTitle;
        private String chatContent;
        private String price;
        private String status;
        private String productName;
    }
}
