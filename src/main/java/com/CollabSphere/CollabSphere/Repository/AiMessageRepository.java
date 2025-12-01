package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.AiMessage;
import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage,Long> {

    List<AiMessage> findBySenderId(Long SenderId);
    List<AiMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
