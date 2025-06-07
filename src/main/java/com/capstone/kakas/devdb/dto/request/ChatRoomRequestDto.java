package com.capstone.kakas.devdb.dto.request;

import lombok.*;

public class ChatRoomRequestDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createChatRoomDto {
//        private Long memberId;
        private String chatRoomTitle;
        private String content;
        private String category;
        private String deliveryFee;
        private String seller;
        private Integer price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomAssignProductDto {
        private Long chatRoomId;
        private String productName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class messageAnalysisDto{
        private Long chatRoomId;
        private String chatRoomTitle;
        private String message;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class aiRequestDto {
        private String productName;
        private String title;
        private String content;
        private String price;
        private String deliveryFee;
        private String message;
    }
}
