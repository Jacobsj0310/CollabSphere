package com.CollabSphere.CollabSphere.Interface;

import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeamServiceInterface {

    @org.springframework.transaction.annotation.Transactional
    TeamDTO.TeamResponse createTeam(TeamDTO.TeamRequest request, String ownerEmail);

    TeamDTO.TeamResponse createTeam(TeamDTO.TeamResponse request, String ownerEmail);

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    TeamDTO.TeamResponse getTeam(Long teamId);

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    List<TeamDTO.TeamResponse> listTeamsForUser(String userEmail);

    @org.springframework.transaction.annotation.Transactional
    TeamDTO.TeamResponse updateTeam(Long teamId, TeamDTO.TeamRequest request, String requesterEmail);

    @org.springframework.transaction.annotation.Transactional
    void deleteTeam(Long teamId, String requesterEmail);

    @org.springframework.transaction.annotation.Transactional
    TeamDTO.TeamResponse addMember(Long teamId, String memberEmail, String requesterEmail);

    @Transactional
    TeamDTO.TeamResponse removeMember(Long teamId, String memberEmail, String requesterEmail);
}
