package com.CollabSphere.CollabSphere.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="teams")

public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private String name;

    private String description;

    //team owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    //Team Members
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name= "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    @Builder.Default
    private Set<User> members = new HashSet<>();

    private Instant createdAt = Instant.now();
    private Instant updatedAt= Instant.now();


    @PrePersist
    public void prePersist(){
        createdAt= Instant.now();
        updatedAt= Instant.now();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt= Instant.now();
    }


}
