package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    List<ChatRoom> findByWorkspaceId(Long workspaceId);
    List<ChatRoom> indByCreatedBy(Long userId);
    List<ChatRoom> findByIsPrivateFalseAndWorkspaceId(Long workspaceId);
}
