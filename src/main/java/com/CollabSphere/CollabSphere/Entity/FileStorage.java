package com.CollabSphere.CollabSphere.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String s3Key;

    private String contentType;

    private Long size;

    //  Single uploader (matches getUploader() and builder().uploader(...))
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")   // FK column in "files" table
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {   // 🔧 this should be @PreUpdate, not @PrePersist
        this.updatedAt = Instant.now();
    }
}