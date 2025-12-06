package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Enum.RoleType;
import com.CollabSphere.CollabSphere.Interface.TeamServiceInterface;
import com.CollabSphere.CollabSphere.Repository.TeamRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TeamService implements TeamServiceInterface {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;


    private TeamDTO.TeamResponse toDto(Team t) {
        TeamDTO.TeamResponse dto = new TeamDTO.TeamResponse();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setDescription(t.getDescription());
        dto.setOwnerId(t.getOwner() != null ? t.getOwner().getId() : null);
        dto.setOwnerName(t.getOwner() != null ? t.getOwner().getName() : null);
        dto.setMemberCount(t.getMembers() != null ? t.getMembers().size() : 0);
        dto.setMemberEmails(t.getMembers().stream().map(User::getEmail).collect(Collectors.toList()));
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public TeamDTO.TeamResponse createTeam(TeamDTO.TeamRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        // add owner as a member
        team.getMembers().add(owner);

        // add invited members (by email) if provided
        if (request.getMemberEmails() != null && !request.getMemberEmails().isEmpty()) {
            for (String email : request.getMemberEmails()) {
                userRepository.findByEmail(email).ifPresent(team.getMembers()::add);
            }
        }

        Team saved = teamRepository.save(team);
        return toDto(saved);
    }

    @Override
    public TeamDTO.TeamResponse createTeam(TeamDTO.TeamResponse request, String ownerEmail) {
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public TeamDTO.TeamResponse getTeam(Long teamId) {
        Team t = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        return toDto(t);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TeamDTO.TeamResponse> listTeamsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Use repository method to fetch teams where this user is a member
        List<Team> teams = teamRepository.findByMembers_Id(user.getId());
        return teams.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TeamDTO.TeamResponse updateTeam(Long teamId, TeamDTO.TeamRequest request, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));

        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only owner or ADMIN can update
        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not authorized to update team");
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        // Optionally update members: if request.memberEmails provided, replace members set (keeping owner)
        if (request.getMemberEmails() != null) {
            User owner = team.getOwner();
            team.getMembers().clear();
            if (owner != null) team.getMembers().add(owner);

            for (String email : request.getMemberEmails()) {
                userRepository.findByEmail(email).ifPresent(u -> {
                    if (!u.getId().equals(owner != null ? owner.getId() : -1L)) {
                        team.getMembers().add(u);
                    }
                });
            }
        }

        Team saved = teamRepository.save(team);
        return toDto(saved);
    }

    @Transactional
    @Override
    public void deleteTeam(Long teamId, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));

        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not authorized to delete team");
        }

        teamRepository.delete(team);
    }

    @Transactional
    @Override
    public TeamDTO.TeamResponse addMember(Long teamId, String memberEmail, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not authorized to add members");
        }

        User member = userRepository.findByEmail(memberEmail).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Optional: enforce team size limit (e.g. 20)
        int MAX_MEMBERS = 20;
        if (team.getMembers().size() >= MAX_MEMBERS) {
            throw new IllegalStateException("Team member limit reached");
        }

        boolean alreadyMember = team.getMembers().stream().anyMatch(u -> u.getId().equals(member.getId()));
        if (!alreadyMember) {
            team.getMembers().add(member);
            Team saved = teamRepository.save(team);
            return toDto(saved);
        } else {
            return toDto(team);
        }
    }

    @Transactional
    @Override
    public TeamDTO.TeamResponse removeMember(Long teamId, String memberEmail, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Not authorized to remove members");
        }

        User member = userRepository.findByEmail(memberEmail).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Prevent removing owner via this method
        if (team.getOwner() != null && team.getOwner().getId().equals(member.getId())) {
            throw new IllegalArgumentException("Cannot remove team owner");
        }

        boolean removed = team.getMembers().removeIf(u -> u.getId().equals(member.getId()));
        if (removed) {
            Team saved = teamRepository.save(team);
            return toDto(saved);
        } else {
            return toDto(team); // no-op if not member
        }
    }
}
