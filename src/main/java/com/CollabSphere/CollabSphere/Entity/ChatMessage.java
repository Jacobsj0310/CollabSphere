package com.CollabSphere.CollabSphere.Entity;
import com.CollabSphere.CollabSphere.Enum.MessageType;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    //message text
    @Column(columnDefinition = "TEXT")
    private String content;

    //url and others
    private String attachmentUrl;

    //sender
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    //for group messages
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    //direct chat
    private String channel;

    //message type
    @Enumerated(EnumType.STRING)
    private MessageType type;

    // message deletion flag
    @Column(nullable = false)
    private boolean deleted = false;


    //timestamps
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt= Instant.now();
        updatedAt=createdAt;
    }

    @PreUpdate

    void onUpdate(){
            updatedAt=Instant.now();
    }

}
