package com.capstone.kakas.devdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.devdb.domain.ChatRoom;
import com.capstone.kakas.devdb.domain.Product;
import com.capstone.kakas.devdb.dto.request.ChatRoomRequestDto;
import com.capstone.kakas.devdb.dto.response.ChatRoomResponseDto;
import com.capstone.kakas.devdb.repository.ChatRoomRepository;
import com.capstone.kakas.devdb.repository.DEVProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final DEVProductRepository productRepository;

    public ChatRoomResponseDto.addChatRoomDto addChatRoom(ChatRoomRequestDto.createChatRoomDto request) {

//        // 요청된 제목을 기반으로, 유사도가 높은 상품 이름 상위 5개를 가져온다
//        List<String> suggestedProductNames = filteringProductName(request.getChatRoomTitle());

        ChatRoom chatRoom = ChatRoom.builder()
                .title(request.getChatRoomTitle())
                .content(request.getContent())
                .seller(request.getSeller())
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return ChatRoomResponseDto.addChatRoomDto.builder()
                .chatRoomId(saved.getId())
//                .suggestedProductNames(suggestedProductNames)
                .build();

    }


    public ChatRoomResponseDto.ChatRoomAssignProductDto assignProductToChatRoom(
            ChatRoomRequestDto.ChatRoomAssignProductDto request
    ) {
        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));
        // Product 조회
        Product product = productRepository.findByName(request.getProductName())
                .orElseThrow(() -> new TempHandler(ErrorStatus.PRODUCT_NOT_FOUND));
        // 연관관계 설정
        chatRoom.setProduct(product);


        // 변경 감지를 위해 save() 호출 (필요 시)
        ChatRoom updated = chatRoomRepository.save(chatRoom);

        // 응답 DTO 생성
        return ChatRoomResponseDto.ChatRoomAssignProductDto.builder()
                .chatRoomId(updated.getId())
                .productId(product.getId())
                .productName(product.getName())
                .build();
    }

}
