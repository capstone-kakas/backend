package com.capstone.kakas.devdb.dto.request;

import com.capstone.kakas.devdb.domain.ChatRoom;
import com.capstone.kakas.devdb.domain.enums.ProductCategory;
import lombok.*;

import java.util.List;

public class ChatRoomRequestDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createChatRoomDto {
        private Long memberId;
        private String chatRoomTitle;
        private String content;
        private Integer category;
        private String deliveryFee;
        private Long seller;
        private String price;
        private String status;
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
    public static class chatSellerDto {
        private Long chatRoomId;
        private String seller_chat;
        private String question;
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class messageAnalysisDto{
        private Long chatRoomId;
        private List<messageRequestDto> message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class messageRequestDto{
        private String text;
        private String sender;
        private String time;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class chatDto{
        private Long chatRoomId;
        private String question;
    }

}
