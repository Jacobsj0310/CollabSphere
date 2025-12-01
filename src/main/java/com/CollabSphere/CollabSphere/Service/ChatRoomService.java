package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import java.util.List;

public interface ChatRoomService {
    ChatRoom createChatRoom(ChatRoom chatRoom);
    ChatRoom getChatRoomById(Long id);
    List<ChatRoom> listByWorkspace(Long workspaceId);
    ChatRoom addParticipant(Long chatRoomId, Long userId);
    ChatRoom removeParticipant(Long chatRoomId, Long userId);
    void deleteChatRoom(Long id);
}
