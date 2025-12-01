package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.AiMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AiMessageService {

    AiMessage saveMessage(AiMessage message);
    AiMessage updateMessage(Long id, AiMessage update);
    void deleteMessage(Long id);
    Optional<AiMessage> getMessageById(Long id);
    List<AiMessage> getMessagesByChatRoom(Long chatRoomId);
    List<AiMessage> getMessagesBySender(Long senderId);

}
