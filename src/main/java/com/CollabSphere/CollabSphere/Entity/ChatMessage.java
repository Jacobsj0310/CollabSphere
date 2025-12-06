package com.CollabSphere.CollabSphere.Entity;

import com.CollabSphere.CollabSphere.Enum.MessageType;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.Instant;
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

    //timestamps
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void CreatedAT(){
        createdAt= Instant.now();
        updatedAt=createdAt;
    }

    @PreUpdate

    void UpdatedAt(){
            updatedAt=Instant.now();
    }

}
