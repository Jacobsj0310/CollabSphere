package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.ChatMessage;
import com.CollabSphere.CollabSphere.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    // find user by email (required for login & JWT authentication)
    // Optional<User> findByEmail(String email);

    Page<ChatMessage> findByTeamIdAndDeletedFalseOrderByCreatedAtAsc(Long teamId, Pageable pageable);

    // find recent messages for a channel within a team (optional channel)
    Page<ChatMessage> findByTeamIdAndChannelAndDeletedFalseOrderByCreatedAtAsc(Long teamId, String channel, Pageable pageable);

    // find direct messages by channel id (if you encode dm channels in channel)
    Page<ChatMessage> findByChannelAndDeletedFalseOrderByCreatedAtAsc(String channel, Pageable pageable);

    // find messages by sender
    List<ChatMessage> findBySenderIdAndDeletedFalseOrderByCreatedAtDesc(Long senderId);


}
