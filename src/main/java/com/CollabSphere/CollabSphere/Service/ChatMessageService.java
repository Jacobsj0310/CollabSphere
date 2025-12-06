package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.ChatMessageDTO;
import com.CollabSphere.CollabSphere.Entity.ChatMessage;
import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Interface.ChatMessageServiceInterface;
import com.CollabSphere.CollabSphere.Repository.ChatMessageRepository;
import com.CollabSphere.CollabSphere.Repository.TeamRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.CollabSphere.CollabSphere.Enum.MessageType;


@Service
@Transactional
public class ChatMessageService implements ChatMessageServiceInterface {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                                  UserRepository userRepository,
                                  TeamRepository teamRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public ChatMessageDTO createMessage(ChatMessageDTO dto) {
        // Validate senderId
        if (dto.getSenderId() == null) {
            throw new IllegalArgumentException("senderId is required");
        }

        // Validate content or attachment
        if ((dto.getContent() == null || dto.getContent().isBlank())
                && (dto.getAttachmentUrl() == null || dto.getAttachmentUrl().isBlank())) {
            throw new IllegalArgumentException("Message content or attachment must be provided");
        }

        // Load sender (User)
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        // Optional team lookup and membership check
        Team team = null;
        if (dto.getTeamId() != null) {
            team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Team not found"));

            // Basic membership check using Team.members collection (if present)
            boolean isMember = team.getMembers() != null && team.getMembers().stream()
                    .anyMatch(u -> u.getId().equals(sender.getId()));
            boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(sender.getId());

            if (!isMember && !isOwner) {
                throw new SecurityException("Sender is not a member of the team");
            }
        }

        // Map DTO type to entity enum (default TEXT)
        MessageType type = MessageType.TEXT;

        if (dto.getType() != null) {
            try {
                type = MessageType.valueOf(dto.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep default
            }
        }


        ChatMessage msg = ChatMessage.builder()
                .content(dto.getContent())
                .attachmentUrl(dto.getAttachmentUrl())
                .sender(sender)
                .team(team)
                .channel(dto.getChannel())
                .type(type)
                .deleted(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(msg);
        return toDto(saved);
    }

@Override
public ChatMessageDTO getMessage(Long messageId) {
    ChatMessage m = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));

    if (m.isDeleted()) {
        throw new EntityNotFoundException("Message not found");
    }

    return toDto(m);
}


    @Override
    public Page<ChatMessageDTO> getMessagesForTeam(Long teamId, java.awt.print.Pageable pageable) {
        return null;
    }

    @Override
    public Page<ChatMessageDTO> getMessagesForChannel(String channel, java.awt.print.Pageable pageable) {
        return null;
    }

    @Override
    public ChatMessageDTO editMessage(Long messageId, Long editorUserId, String newContent) {
        return null;
    }

    @Override
    public Page<ChatMessageDTO> getMessagesForTeam(Long teamId, Pageable pageable) {
        Page<ChatMessage> page = chatMessageRepository.findByTeamIdAndDeletedFalseOrderByCreatedAtAsc(teamId, (java.awt.print.Pageable) pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<ChatMessageDTO> getMessagesForChannel(String channel, Pageable pageable) {
        Page<ChatMessage> page = chatMessageRepository.findByChannelAndDeletedFalseOrderByCreatedAtAsc(channel, (java.awt.print.Pageable) pageable);
        return page.map(this::toDto);
    }

    @Override
    public ChatMessageDTO editMessage(Long messageId, Long editorUserId, ChatMessageDTO dto) {
        ChatMessage m = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        if (m.isDeleted()) throw new EntityNotFoundException("Message not found");

        // Permission: author or team owner

        boolean isAuthor = m.getSender()!=null && m.getSender().getId().equals(editorUserId);
        boolean isTeamOwner = m.getTeam()!= null && m.getTeam().getOwner()!=null &&
                m.getTeam().getOwner().getId().equals(editorUserId);


        if (!isAuthor && !isTeamOwner) {
            throw new SecurityException("Not authorized to edit this message");
        }

        // Update allowed fields (only if present)
        if (dto.getContent() != null) {
            m.setContent(dto.getContent());
        }
        if (dto.getAttachmentUrl() != null) {
            m.setAttachmentUrl(dto.getAttachmentUrl());
        }
        if (dto.getChannel() != null) {
            m.setChannel(dto.getChannel());
        }
        if (dto.getType() != null) {
            try {
                m.setType(MessageType.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid type
            }
        }

        ChatMessage updated = chatMessageRepository.save(m);
        return toDto(updated);
    }

    @Override
    public void deleteMessage(Long messageId, Long requestorUserId) {
        ChatMessage m = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (m.isDeleted()) return;

        boolean isAuthor = m.getSender() != null && m.getSender().getId().equals(requestorUserId);
        boolean isTeamOwner = m.getTeam() != null && m.getTeam().getOwner() != null &&
                m.getTeam().getOwner().getId().equals(requestorUserId);

        if (!isAuthor && !isTeamOwner) {
            throw new SecurityException("Not authorized to delete this message");
        }

        m.setDeleted(true);
        chatMessageRepository.save(m);
    }

    /* Helper to map entity -> DTO (populates both input & output fields on the merged DTO) */
    private ChatMessageDTO toDto(ChatMessage m) {
        Long senderId = m.getSender() != null ? m.getSender().getId() : null;
        String senderEmail = m.getSender() != null ? m.getSender().getEmail() : null;
        Long teamId = m.getTeam() != null ? m.getTeam().getId() : null;

        return ChatMessageDTO.builder()
                .id(m.getId())
                .content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl())
                .senderId(senderId)
                .senderEmail(senderEmail)
                .teamId(teamId)
                .channel(m.getChannel())
                .type(m.getType() != null ? m.getType().name() : null)
                .deleted(m.isDeleted())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}