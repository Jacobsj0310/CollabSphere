package com.CollabSphere.CollabSphere.Entity;

import com.CollabSphere.CollabSphere.Enum.TeamMemberRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many team members belong to ONE team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Many team memberships belong to ONE user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional: useful metadata
    @Column(nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    // Assign role inside the team (not global role)
    @Enumerated(EnumType.STRING)
    private TeamMemberRole role = TeamMemberRole.MEMBER;
}
