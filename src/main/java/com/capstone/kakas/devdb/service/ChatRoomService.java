package com.capstone.kakas.devdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.devdb.domain.*;
import com.capstone.kakas.devdb.dto.request.ChatRoomRequestDto;
import com.capstone.kakas.devdb.dto.response.ChatRoomResponseDto;
import com.capstone.kakas.devdb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final DEVProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ChatAnalysisRepository chatAnalysisRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponseDto.addChatRoomDto addChatRoom(ChatRoomRequestDto.createChatRoomDto request) {
        Member newMember = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.MEMBER_NOT_FOUND));

//        // 요청된 제목을 기반으로, 유사도가 높은 상품 이름 상위 5개를 가져온다
//        List<String> suggestedProductNames = filteringProductName(request.getChatRoomTitle());
        //임시 suggestedProductNames
        List<String> suggestedProductNames = new ArrayList<>(
                List.of("플스5 슬림 디지털", "플스5 프로", "플스5 디스크")
        );

        ChatRoom chatRoom = ChatRoom.builder()
                .member(newMember)
                .title(request.getChatRoomTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .deliveryFee(request.getDeliveryFee())
                .seller(request.getSeller())
                .price(String.valueOf(request.getPrice()))
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return ChatRoomResponseDto.addChatRoomDto.builder()
                .chatRoomId(saved.getId())
                .suggestedProductNames(suggestedProductNames)
                .build();

    }

    @Transactional
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

    @Transactional
    //특정 채팅방의 메세지를 분석하는 service
//    public ChatRoomResponseDto.messageAnalysisResultDto messageAnalysis(ChatRoomRequestDto.messageAnalysisDto request){
    public String messageAnalysis(ChatRoomRequestDto.messageAnalysisDto request){

        // ChatRoom 조회 title로
        ChatRoom chatRoom = chatRoomRepository.findByTitle(request.getChatRoomTitle())
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));

        // ChatRoom 조회 id로 -> 프론트와 협의
//        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
//                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));


        // ai 앤드포인트를 기준으로 분석 결과 가져오기 아직 미구현
//        ChatRoomRequestDto.aiRequestDto aiRequestDto = ChatRoomRequestDto.aiRequestDto.builder()
//                .productName(chatRoom.getProduct().getName())
//                .title(chatRoom.getTitle())
//                .content(chatRoom.getContent())
//                .price(chatRoom.getPrice())
//                .deliveryFee(chatRoom.getDeliveryFee())
//                .message(request.getMessage())
//                .build();
////        aiRequestDto를 ai api로 전송 후 analysisResult 받아오기
//        String analysisResult =

        String analysisResult = "분석결과 temp";


        // 채팅 메세지 저장
        ChatMessage message = ChatMessage.builder()
                .message(request.getMessage())
//                .chatAnalyses(chatAnalysis)
                .build();

        chatRoom.addChatMessage(message);


        // chatAnalysis, chatMessage 저장 및 연관관계
        ChatAnalysis chatAnalysis = ChatAnalysis.builder()
                .analysis(analysisResult)
                .build();
        message.addChatAnalysis(chatAnalysis);


        //cascade때매 다 저장
        chatRoomRepository.save(chatRoom);


        return analysisResult;
    }
}
