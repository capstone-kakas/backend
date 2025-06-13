package com.capstone.kakas.devdb.repository;

import com.capstone.kakas.devdb.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByTitle(String chatRoomTitle);
    List<ChatRoom> findAllByTitle(String title);
}
