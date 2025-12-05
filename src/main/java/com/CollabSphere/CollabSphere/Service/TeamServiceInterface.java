package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TeamServiceInterface {
    @Transactional
    TeamDTO.TeamResponse createTeam(TeamDTO.TeamRequest request, String ownerEmail);

    @Transactional(readOnly = true)
    TeamDTO.TeamResponse getTeam(Long teamId);

    @Transactional(readOnly = true)
    List<TeamDTO.TeamResponse> listTeamsForUser(String userEmail);

    @Transactional
    TeamDTO.TeamResponse updateTeam(Long teamId, TeamDTO.TeamRequest request, String requesterEmail);

    @Transactional
    void deleteTeam(Long teamId, String requesterEmail);

    @Transactional
    TeamDTO.TeamResponse addMember(Long teamId, String memberEmail, String requesterEmail);
}
