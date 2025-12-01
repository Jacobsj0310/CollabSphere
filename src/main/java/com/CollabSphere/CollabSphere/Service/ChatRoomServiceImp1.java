package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.ChatRoomRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import com.CollabSphere.CollabSphere.Service.ChatRoomService;
import com.CollabSphere.CollabSphere.exception.CustomException;
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
                .orElseThrow(() -> new CustomException("ChatRoom not found: " + id));
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
                .orElseThrow(() -> new CustomException("User not found: " + userId));
        chatRoom.addParticipant(user);
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    @Transactional
    public ChatRoom removeParticipant(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found: " + userId));
        chatRoom.removeParticipant(user);
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public void deleteChatRoom(Long id) {
        chatRoomRepository.deleteById(id);
    }
}