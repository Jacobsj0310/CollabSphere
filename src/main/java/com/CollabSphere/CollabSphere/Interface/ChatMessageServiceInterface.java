package com.CollabSphere.CollabSphere.Interface;

import com.CollabSphere.CollabSphere.DTO.ChatMessageDTO;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface ChatMessageServiceInterface {
    ChatMessageDTO createMessage(ChatMessageDTO dto);

    ChatMessageDTO getMessage(Long messageId);

    Page<ChatMessageDTO> getMessagesForTeam(Long teamId, Pageable pageable);

    Page<ChatMessageDTO> getMessagesForChannel(String channel, Pageable pageable);

    ChatMessageDTO editMessage(Long messageId, Long editorUserId, String newContent);

    Page<ChatMessageDTO> getMessagesForTeam(Long teamId, org.springframework.data.domain.Pageable pageable);

    Page<ChatMessageDTO> getMessagesForChannel(String channel, org.springframework.data.domain.Pageable pageable);

    ChatMessageDTO editMessage(Long messageId, Long editorUserId, ChatMessageDTO dto);

    void deleteMessage(Long messageId, Long requestorUserId);
}
