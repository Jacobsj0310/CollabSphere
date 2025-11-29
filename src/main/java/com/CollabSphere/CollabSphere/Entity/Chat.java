package com.CollabSphere.CollabSphere.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

    @Entity
    @Table(name = "chats", indexes = {
            @Index(columnList = "workspaceId"),
            @Index(columnList = "createdBy")
    })
    public class Chat {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(length = 200)
        private String name;

        @Column(nullable = true)
        private Long workspaceId;

        @Column(nullable = false)
        private boolean isPrivate = false;

        private Long createdBy;

        private Integer participantCount = 0;

        private Instant lastMessageAt;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "chat_participants",
                joinColumns = @JoinColumn(name = "chat_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id")
        )
        private Set<User> participants = new HashSet<>();

        private Instant createdAt;

        @PrePersist
        public void prePersist() {
            if (createdAt == null) createdAt = Instant.now();
            if (participantCount == null) participantCount = (participants == null ? 0 : participants.size());
        }

        public void addParticipant(User user) {
            if (participants == null) participants = new HashSet<>();
            if (participants.add(user)) {
                participantCount = participants.size();
            }
        }

        public void removeParticipant(User user) {
            if (participants != null && participants.remove(user)) {
                participantCount = participants.size();
            }
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getWorkspaceId() {
            return workspaceId;
        }

        public void setWorkspaceId(Long workspaceId) {
            this.workspaceId = workspaceId;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
        }

        public Long getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(Long createdBy) {
            this.createdBy = createdBy;
        }

        public Integer getParticipantCount() {
            return participantCount;
        }

        public void setParticipantCount(Integer participantCount) {
            this.participantCount = participantCount;
        }

        public Instant getLastMessageAt() {
            return lastMessageAt;
        }

        public void setLastMessageAt(Instant lastMessageAt) {
            this.lastMessageAt = lastMessageAt;
        }

        public Set<User> getParticipants() {
            return participants;
        }

        public void setParticipants(Set<User> participants) {
            this.participants = participants;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

