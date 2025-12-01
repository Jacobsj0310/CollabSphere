package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.ChatRoomRequestDTO;
import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Repository.ChatRoomRepository;

import java.util.List;

public interface ChatRoomService {
    ChatRoom createChatRoom(ChatRoom chatRoom);

    static ChatRoom getChatRoomById(Long id) {
        return null;
    }

    ChatRoom getChatRoomById(Long id);

    //ChatRoom getChatRoomById(Long id);
    List<ChatRoom> listByWorkspace(Long workspaceId);
    ChatRoom addParticipant(Long chatRoomId, Long userId);
    ChatRoom removeParticipant(Long chatRoomId, Long userId);
    ChatRoom deleteChatRoom(Long id);
    ChatRoom archiveChat(Long id);
    ChatRoom pinChat(Long id);

    void transferOwnership(Long id, Long newOwnerId);

    List<ChatRoom> getByWorkspaceId(Long workspaceId);

    ChatRoom updateChatRoom(Long id, ChatRoomRequestDTO req);
}
