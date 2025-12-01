package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.AiMessage;
import com.CollabSphere.CollabSphere.Repository.AiMessageRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AiMessageServiceImp1 implements AiMessageService{

    private  final AiMessageRepository aiMessageRepository;

    @Override
    @Transactional
    public AiMessage saveMessage(AiMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(Instant.now());
        }
        return aiMessageRepository.save(message);
    }

    @Override
    @Transactional
    public AiMessage updateMessage(Long id, AiMessage update) {
        AiMessage existing = aiMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AiMessage not found with id: " + id));

        if (update.getContent() != null) existing.setContent(update.getContent());
        if (update.getAttachmentUrl() != null) existing.setAttachmentUrl(update.getAttachmentUrl());
        if (update.getReplyToMessageId() != null) existing.setReplyToMessageId(update.getReplyToMessageId());
        if (update.getType() != null) existing.setType(update.getType());

        return aiMessageRepository.save(existing);
    }


    @Override
    @Transactional
    public void deleteMessage(Long id) {
        if (!aiMessageRepository.existsById(id)) {
            throw new RuntimeException("AiMessage not found with id: " + id);
        }
        aiMessageRepository.deleteById(id);
    }

    @Override
    public Optional<AiMessage> getMessageById(Long id) {
        return aiMessageRepository.findById(id);
    }

    @Override
    public List<AiMessage> getMessagesByChatRoom(Long chatRoomId) {
        return aiMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
    }

    @Override
    public List<AiMessage> getMessagesBySender(Long senderId) {
        return aiMessageRepository.findBySenderId(senderId);
    }

}
