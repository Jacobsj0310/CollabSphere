package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.ChatRoomRequestDTO;
import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import java.util.List;

public interface ChatRoomService {
    ChatRoom createChatRoom(ChatRoom chatRoom);

    static ChatRoom getChatRoomById(Long id) {
        return null;
    }

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
