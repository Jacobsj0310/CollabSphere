package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.ChatRoomRequestDTO;
import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.ChatRoomRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;


    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        // initialize participant count and createdAt will be handled by entity @PrePersist
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoom getChatRoomById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found: " + id));
    }

    @Override
    public List<ChatRoom> listByWorkspace(Long workspaceId) {
        return chatRoomRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public ChatRoom addParticipant(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        chatRoom.addParticipant(user);
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    @Transactional
    public ChatRoom removeParticipant(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        chatRoom.removeParticipant(user);
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoom archiveChat(Long id) {
        return null;
    }

    @Override
    public ChatRoom pinChat(Long id) {
        return null;
    }

    @Override
    public void transferOwnership(Long id, Long newOwnerId) {

    }

    @Override
    public List<ChatRoom> getByWorkspaceId(Long workspaceId) {
        return List.of();
    }

    @Override
    public ChatRoom updateChatRoom(Long id, ChatRoomRequestDTO req) {
        return null;
    }

    @Override
    public ChatRoom deleteChatRoom(Long id) {
        chatRoomRepository.deleteById(id);
        return null;
    }
}